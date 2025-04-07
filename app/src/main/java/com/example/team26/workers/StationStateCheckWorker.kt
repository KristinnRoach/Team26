package com.example.team26.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
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
import com.example.team26.notifications.SystemNotificationManager
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class StationStateCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val stationRepository: StationRepository = AppModule.stationRepository
    private val notificationDao: NotificationDao = AppModule.notificationDao
    private val preferenceManager: NotificationPreferenceManager = AppModule.notificationPreferenceManager
    private val apiSettingsManager: ApiSettingsManager = AppModule.apiSettingsManager

    private val systemNotificationManager = SystemNotificationManager(appContext)

    companion object {
        const val WORK_NAME = "StationStateCheckWorker"
        private const val TAG = "StationStateCheckWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Worker starting check...")

        var apiCheckException: Exception? = null
        var stationsProcessedSuccessfully = false
        var refreshCallResult: kotlin.Result<Unit>? = null
        var workExecutionResult: Result = Result.success()

        try {
            Log.d(TAG, "Worker attempting repository.refreshStations()")
            refreshCallResult = stationRepository.refreshStations()
            Log.d(TAG, "Worker repository.refreshStations() completed. Success: ${refreshCallResult.isSuccess}")

            refreshCallResult.onFailure { e ->
                apiCheckException = e as? Exception ?: Exception("Unknown refresh failure from Result")
                Log.w(TAG, "Worker error during API check (extracted from Result): ${e.message}")
            }

            refreshCallResult.onSuccess {
                Log.d(TAG, "Worker processing station changes as refresh was successful.")
                val currentApiStations = stationRepository.getAllStations().first()
                processStationChanges(currentApiStations)
                stationsProcessedSuccessfully = true
            }

        } catch (e: Exception) {
            apiCheckException = e
            Log.e(TAG, "Worker unexpected error during check cycle: ${e.message}", e)
            workExecutionResult = Result.failure()
        }

        val currentlyReachable = determineReachability(apiCheckException)
        Log.d(TAG, "Worker determined currentlyReachable = $currentlyReachable based on exception: ${apiCheckException?.javaClass?.simpleName}")

        if (workExecutionResult != Result.failure()) {
            if (apiCheckException != null) {
                workExecutionResult = if (currentlyReachable) {
                    Log.w(TAG, "API might be reachable, but Result was failure or unexpected error occurred. Marking work as failed.")
                    Result.failure()
                } else {
                    Log.w(TAG, "API connectivity error detected. Marking work for retry.")
                    Result.retry()
                }
            } else {
                Log.d(TAG, "API check and processing successful. Marking work as success.")
                workExecutionResult = Result.success()
            }
        }

        processApiStatusChange(currentlyReachable, apiCheckException)

        Log.d(TAG, "Worker finished with final work result: $workExecutionResult")
        return workExecutionResult
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
                Log.w(TAG, "Unhandled exception type (${exception::class.java.simpleName}) treated as unreachable.")
                false
            }
        }
    }

    private suspend fun processStationChanges(currentStations: List<StationEntity>) {
        try {
            val lastKnownStates = notificationDao.getAllLastKnownStates()
            val lastKnownStateMap = lastKnownStates.associateBy { it.id }
            val currentStationMap = currentStations.associateBy { it.id }
            var newNotificationCount = 0
            val notificationMessagesForSystem = mutableListOf<String>()

            currentStations.forEach { currentStation ->
                val lastKnown = lastKnownStateMap[currentStation.id]
                val preference = NotificationMapper.mapComparisonToPreference(currentStation, lastKnown)
                if (preference != null && preference != NotificationPreference.API_STATUS_CHANGE && preferenceManager.isPreferenceEnabled(preference)) {
                    val message = constructChangeMessage(currentStation, lastKnown, preference)
                    logNotification(message, mapPreferenceToType(preference), currentStation.id)?.let { loggedMsg ->
                        newNotificationCount++
                        notificationMessagesForSystem.add(loggedMsg)
                    }
                }
            }
            if (preferenceManager.isPreferenceEnabled(NotificationPreference.STATION_ADDED_REMOVED)) {
                lastKnownStates.forEach { lastKnown ->
                    if (!currentStationMap.containsKey(lastKnown.id)) {
                        val message = "Station '${lastKnown.name}' may be offline or removed."
                        logNotification(message, NotificationType.STATION_REMOVED, lastKnown.id)?.let { loggedMsg ->
                            newNotificationCount++
                            notificationMessagesForSystem.add(loggedMsg)
                        }
                    }
                }
            }

            // Update DB with latest states
            val newStatesToStore = currentStations.map { it.toLastKnownStateEntity() }
            notificationDao.updateAllLastKnownStates(newStatesToStore)

            // Send System Notification for Station Changes (if any)
            if (newNotificationCount > 0) {
                Log.d(TAG, "Showing System Notification for $newNotificationCount station changes.")
                systemNotificationManager.showSummaryNotification(
                    title = "Station Updates",
                    summaryText = "$newNotificationCount station(s) updated.",
                    details = notificationMessagesForSystem
                )
            } else {
                Log.d(TAG, "No relevant station changes detected or preferences disabled for system notification.")
            }
        } catch(dbError: Exception) {
            Log.e(TAG, "Worker error during station change processing or DB update: ${dbError.message}", dbError)
        }
    }


    // Processes API status changes - Calls System Notif Manager
    private suspend fun processApiStatusChange(currentlyReachable: Boolean, exception: Exception?) {
        val lastKnownReachable = apiSettingsManager.getLastKnownApiReachableState()
        Log.d(TAG, "Worker processApiStatusChange - Current: $currentlyReachable, Last Known: $lastKnownReachable")

        if (currentlyReachable != lastKnownReachable) {
            Log.i(TAG, "Worker API status change DETECTED! From $lastKnownReachable to $currentlyReachable")
            // Update the stored state immediately
            apiSettingsManager.setLastKnownApiReachableState(currentlyReachable)
            Log.d(TAG, "Worker stored state updated to: ${apiSettingsManager.getLastKnownApiReachableState()}")

            // Check if user wants this specific notification type
            if (preferenceManager.isPreferenceEnabled(NotificationPreference.API_STATUS_CHANGE)) {
                Log.d(TAG, "Worker API Status Change preference IS enabled.")
                val message: String
                val type: NotificationType
                val systemNotificationTitle: String
                val systemNotificationDetails: List<String>

                if (currentlyReachable) {
                    message = "API connection restored."
                    type = NotificationType.INFO
                    systemNotificationTitle = "API Status"
                    systemNotificationDetails = listOf(message)
                } else {
                    message = when (exception) {
                        is ConnectException, is UnknownHostException, is SocketTimeoutException -> "API Unreachable. Check connection or endpoint."
                        is HttpException -> "API Error (${exception.code()}). Check server."
                        else -> "API check failed unexpectedly (${exception?.message ?: "Unknown reason"})."
                    }
                    type = NotificationType.SYNC_ERROR
                    systemNotificationTitle = "API Issue"
                    systemNotificationDetails = listOf(message, "Will retry background check later.")
                }
                logNotification(message, type, null)

                Log.d(TAG, "Showing System Notification for API status change: '$message'")
                systemNotificationManager.showSummaryNotification(
                    title = systemNotificationTitle,
                    summaryText = message,
                    details = systemNotificationDetails
                )
            } else {
                Log.w(TAG, "Worker API Status Change preference IS DISABLED.")
            }
        } else {
            Log.d(TAG, "Worker no change in API status ($currentlyReachable). No status change notification needed.")
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
                Log.i(TAG, "Worker logged notification to DB: [$type] $message (ID: ${stationId ?: "N/A"})")
                message
            } else {
                Log.w(TAG, "Worker failed to insert notification into DB (returned ID <= 0).")
                null
            }
        } catch (dbError: Exception) {
            Log.e(TAG, "Worker failed to log notification to DB: ${dbError.message}", dbError)
            null
        }
    }

    private fun constructChangeMessage(current: StationEntity, lastKnown: LastKnownStationStateEntity?, preference: NotificationPreference): String {
        return when(preference) {
            NotificationPreference.STATION_ERROR -> "Station '${current.name}' entered ERROR state (${if(current.state == DeviceState.ERROR) "State: ${current.state}" else "Mode: ${current.controlMode}"})."
            NotificationPreference.STATION_RECOVERY -> "Station '${current.name}' recovered from ERROR (Now: State=${current.state}, Mode=${current.controlMode})."
            NotificationPreference.STATE_CHANGE -> "Station '${current.name}' state changed to ${current.state}."
            NotificationPreference.MODE_CHANGE -> "Station '${current.name}' mode changed to ${current.controlMode}."
            NotificationPreference.STATION_ADDED_REMOVED -> if(lastKnown == null) "New station detected: '${current.name}'." else "Station '${current.name}' status changed (Specific pref: ${preference.name})."
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