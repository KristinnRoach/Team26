package com.example.team26.utils

import android.util.Log
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.team26.data.preferences.ApiSettingsManager
import com.example.team26.data.preferences.NotificationPreferenceManager
import com.example.team26.di.AppModule
import com.example.team26.domain.notification.LastKnownStationStateEntity
import com.example.team26.domain.notification.NotificationComparator.toLastKnownStateEntity
import com.example.team26.domain.notification.NotificationDao
import com.example.team26.domain.notification.NotificationEntity
import com.example.team26.domain.notification.NotificationMapper
import com.example.team26.domain.notification.NotificationPreference
import com.example.team26.domain.notification.NotificationType
import com.example.team26.domain.station.DeviceState
import com.example.team26.domain.station.StationEntity
import com.example.team26.domain.station.StationRepository
import com.example.team26.ui.station.StationViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

private const val TAG_CHECKER = "ForegroundChecker"
private val CHECK_INTERVAL_MILLIS = TimeUnit.SECONDS.toMillis(5)

class ForegroundStateChecker(
    private val lifecycleOwner: LifecycleOwner,
    private val stationRepository: StationRepository,
    private val notificationDao: NotificationDao,
    private val stationViewModel: StationViewModel,
    private val showSnackbar: suspend (String, String?) -> SnackbarResult
) {

    private var job: Job? = null
    private val preferenceManager: NotificationPreferenceManager = AppModule.notificationPreferenceManager
    private val apiSettingsManager: ApiSettingsManager = AppModule.apiSettingsManager

    fun start() {
        if (job?.isActive == true) {
            Log.d(TAG_CHECKER, "Checker already running.")
            return
        }
        job = lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d(TAG_CHECKER, "Checker starting or resuming...")
                stationViewModel.setApiReachable(apiSettingsManager.getLastKnownApiReachableState())

                while (isActive) {
                    var apiCheckException: Exception? = null
                    var stationsProcessedSuccessfully = false
                    var refreshCallResult: kotlin.Result<Unit>? = null

                    try {
                        Log.d(TAG_CHECKER, "Attempting repository.refreshStations()")
                        refreshCallResult = stationRepository.refreshStations()
                        Log.d(TAG_CHECKER, "repository.refreshStations() completed. Success: ${refreshCallResult.isSuccess}")

                        refreshCallResult.onFailure { e ->
                            apiCheckException = e as? Exception ?: Exception("Unknown refresh failure")
                            Log.w(TAG_CHECKER, "Error during API check (extracted from Result): ${e.message}")
                        }

                        refreshCallResult.onSuccess {
                            Log.d(TAG_CHECKER, "Processing station changes as refresh was successful.")
                            val currentStations = stationRepository.getAllStations().first()
                            processStationChanges(currentStations)
                            stationsProcessedSuccessfully = true
                        }

                    } catch (e: Exception) {
                        apiCheckException = e
                        Log.e(TAG_CHECKER, "Unexpected error during check cycle: ${e.message}", e)
                    }

                    val currentlyReachable = determineReachability(apiCheckException)
                    Log.d(TAG_CHECKER, "Determined currentlyReachable = $currentlyReachable based on exception: ${apiCheckException?.javaClass?.simpleName}")

                    processApiStatusChange(currentlyReachable, apiCheckException)

                    stationViewModel.setApiReachable(currentlyReachable)

                    Log.d(TAG_CHECKER, "Check cycle complete, delaying for ${CHECK_INTERVAL_MILLIS}ms")
                    delay(CHECK_INTERVAL_MILLIS)
                }
            }
            Log.d(TAG_CHECKER, "Checker stopping (Lifecycle not STARTED).")
        }
    }

    private fun determineReachability(exception: Exception?): Boolean {
        return when (exception) {
            null -> true // No exception means success
            is ConnectException,
            is UnknownHostException,
            is SocketTimeoutException,
            is HttpException -> false
            else -> {
                // Returning false treats any error as unreachable for status change purposes.
                Log.w(TAG_CHECKER, "Unhandled exception type (${exception::class.java.simpleName}) treated as unreachable.")
                false
            }
        }
    }

    private suspend fun processStationChanges(currentStations: List<StationEntity>) {
        try {
            val lastKnownStates = notificationDao.getAllLastKnownStates()
            val lastKnownStateMap = lastKnownStates.associateBy { it.id }
            val currentStationMap = currentStations.associateBy { it.id }
            var newNotificationsLogged = 0
            val changeMessages = mutableListOf<String>()

            currentStations.forEach { currentStation ->
                val lastKnown = lastKnownStateMap[currentStation.id]
                val preference = NotificationMapper.mapComparisonToPreference(currentStation, lastKnown)
                if (preference != null && preference != NotificationPreference.API_STATUS_CHANGE && preferenceManager.isPreferenceEnabled(preference)) {
                    val message = constructChangeMessage(currentStation, lastKnown, preference)
                    logNotification(message, mapPreferenceToType(preference), currentStation.id)?.let { msg ->
                        newNotificationsLogged++
                        changeMessages.add(msg)
                    }
                }
            }
            if (preferenceManager.isPreferenceEnabled(NotificationPreference.STATION_ADDED_REMOVED)) {
                lastKnownStates.forEach { lastKnown ->
                    if (!currentStationMap.containsKey(lastKnown.id)) {
                        val message = "Station '${lastKnown.name}' may be offline or removed."
                        logNotification(message, NotificationType.STATION_REMOVED, lastKnown.id)?.let { msg ->
                            newNotificationsLogged++
                            changeMessages.add(msg)
                        }
                    }
                }
            }
            // Update DB with latest states
            val newStatesToStoreInDb = currentStations.map { it.toLastKnownStateEntity() }
            notificationDao.updateAllLastKnownStates(newStatesToStoreInDb)

            if (newNotificationsLogged > 0) {
                val summary = if (newNotificationsLogged == 1) changeMessages.first() else "$newNotificationsLogged station updates"
                Log.d(TAG_CHECKER, "Showing Snackbar for station changes: '$summary'")
                showSnackbar(summary, "View")
            } else {
                Log.d(TAG_CHECKER, "No relevant station changes detected or preferences disabled.")
            }
        } catch(dbError: Exception) {
            Log.e(TAG_CHECKER, "Error during station change processing or DB update: ${dbError.message}", dbError)
        }
    }


    private suspend fun processApiStatusChange(currentlyReachable: Boolean, exception: Exception?) {
        val lastKnownReachable = apiSettingsManager.getLastKnownApiReachableState()
        Log.d(TAG_CHECKER, "processApiStatusChange - Current: $currentlyReachable, Last Known: $lastKnownReachable")

        if (currentlyReachable != lastKnownReachable) {
            Log.i(TAG_CHECKER, "API status change DETECTED! From $lastKnownReachable to $currentlyReachable")
            // Update the stored state immediately
            apiSettingsManager.setLastKnownApiReachableState(currentlyReachable)
            Log.d(TAG_CHECKER, "Stored state updated to: ${apiSettingsManager.getLastKnownApiReachableState()}")

            // Check if user wants this specific notification type
            if (preferenceManager.isPreferenceEnabled(NotificationPreference.API_STATUS_CHANGE)) {
                Log.d(TAG_CHECKER, "API Status Change preference IS enabled.")
                val message: String
                val type: NotificationType
                if (currentlyReachable) {
                    message = "API connection restored."
                    type = NotificationType.INFO
                } else {
                    message = when (exception) {
                        is ConnectException, is UnknownHostException, is SocketTimeoutException -> "API Unreachable. Check connection or endpoint."
                        is HttpException -> "API Error (${exception.code()}). Check server."
                        else -> "API check failed unexpectedly (${exception?.message ?: "Unknown reason"})."
                    }
                    type = NotificationType.SYNC_ERROR
                }
                logNotification(message, type, null)
                Log.d(TAG_CHECKER, "Showing Snackbar for API status change: '$message'")
                showSnackbar(message, null)
            } else {
                Log.w(TAG_CHECKER, "API Status Change preference IS DISABLED.")
            }
        } else {
            Log.d(TAG_CHECKER, "No change in API status ($currentlyReachable). No status change notification needed.")
        }
    }

    private suspend fun logNotification(message: String, type: NotificationType, stationId: String?): String? {
        return try {
            val notification = NotificationEntity(
                timestamp = System.currentTimeMillis(),
                message = message,
                type = type,
                stationId = stationId,
                isRead = false
            )
            val insertedId = notificationDao.insertNotification(notification)
            if (insertedId > 0) {
                Log.i(TAG_CHECKER, "Logged notification to DB: [$type] $message (ID: ${stationId ?: "N/A"})")
                message
            } else {
                Log.w(TAG_CHECKER, "Failed to insert notification into DB (returned ID <= 0).")
                null
            }
        } catch (dbError: Exception) {
            Log.e(TAG_CHECKER, "Failed to log notification to DB: ${dbError.message}", dbError)
            null
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private fun constructChangeMessage(current: StationEntity, lastKnown: LastKnownStationStateEntity?, preference: NotificationPreference): String {
        return when(preference) {
            NotificationPreference.STATION_ERROR -> "Station '${current.name}' entered ERROR state (${if(current.state == DeviceState.ERROR) "State: ${current.state}" else "Mode: ${current.controlMode}"})."
            NotificationPreference.STATION_RECOVERY -> "Station '${current.name}' recovered from ERROR (Now: State=${current.state}, Mode=${current.controlMode})."
            NotificationPreference.STATE_CHANGE -> "Station '${current.name}' state changed to ${current.state}."
            NotificationPreference.MODE_CHANGE -> "Station '${current.name}' mode changed to ${current.controlMode}."
            NotificationPreference.STATION_ADDED_REMOVED -> if(lastKnown == null) "New station detected: '${current.name}'." else "Station '${current.name}' status changed (Specific pref: ${preference.name})." // Fallback message adjusted
            else -> "Station '${current.name}' updated." // Should ideally not hit this for defined prefs
        }
    }

    private fun mapPreferenceToType(preference: NotificationPreference): NotificationType {
        return when(preference) {
            // API_STATUS_CHANGE is handled separately in processApiStatusChange
            NotificationPreference.STATION_ERROR -> NotificationType.STATE_CHANGE
            NotificationPreference.STATION_RECOVERY -> NotificationType.STATE_CHANGE
            NotificationPreference.STATE_CHANGE -> NotificationType.STATE_CHANGE
            NotificationPreference.MODE_CHANGE -> NotificationType.MODE_CHANGE
            NotificationPreference.STATION_ADDED_REMOVED -> NotificationType.STATION_ADDED
            else -> NotificationType.INFO
        }
    }
}