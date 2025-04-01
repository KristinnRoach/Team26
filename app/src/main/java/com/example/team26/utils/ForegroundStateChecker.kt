package com.example.team26.utils

import android.util.Log
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.team26.data.preferences.NotificationPreferenceManager
import com.example.team26.di.AppModule
import com.example.team26.domain.notification.LastKnownStationStateEntity
import com.example.team26.domain.notification.NotificationComparator.toLastKnownStateEntity
import com.example.team26.domain.notification.NotificationDao
import com.example.team26.domain.notification.NotificationEntity
import com.example.team26.domain.notification.NotificationMapper
import com.example.team26.domain.notification.NotificationPreference
import com.example.team26.domain.notification.NotificationType
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

    fun start() {
        if (job?.isActive == true) return
        job = lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                stationViewModel.setApiReachable(true)

                while (isActive) {
                    var apiRefreshSuccess = false
                    var currentStations: List<StationEntity>? = null
                    var lastKnownStates: List<LastKnownStationStateEntity> = emptyList()
                    var lastKnownStateMap: Map<String, LastKnownStationStateEntity> = emptyMap()

                    try {
                        // Refresh stations
                        stationRepository.refreshStations()
                        apiRefreshSuccess = true
                        stationViewModel.setApiReachable(true)

                        currentStations = stationRepository.getAllStations().first()
                        val currentStationMap = currentStations?.associateBy { it.id } ?: emptyMap()

                        lastKnownStates = notificationDao.getAllLastKnownStates()
                        lastKnownStateMap = lastKnownStates.associateBy { it.id }

                        if (currentStations == null) {
                            throw IllegalStateException("Failed to get station data.")
                        }

                        var newNotificationsLogged = 0
                        val changeMessages = mutableListOf<String>()
                        currentStations.forEach { currentStation ->
                            val lastKnown = lastKnownStateMap[currentStation.id]
                            val preference = NotificationMapper.mapComparisonToPreference(currentStation, lastKnown)

                            if (preference != null && preferenceManager.isPreferenceEnabled(preference)) {
                                val message = constructChangeMessage(currentStation, lastKnown, preference)
                                val notification = NotificationEntity(
                                    timestamp = System.currentTimeMillis(), message = message,
                                    type = mapPreferenceToType(preference), stationId = currentStation.id, isRead = false
                                )
                                val insertedId = notificationDao.insertNotification(notification)
                                if (insertedId > 0) {
                                    newNotificationsLogged++
                                    changeMessages.add(message)
                                }
                            }
                        }

                        if (preferenceManager.isPreferenceEnabled(NotificationPreference.STATION_ADDED_REMOVED)) {
                            lastKnownStates.forEach { lastKnown ->
                                if (!currentStationMap.containsKey(lastKnown.id)) {
                                    val message = "Station '${lastKnown.name}' may be offline or removed."
                                    val notification = NotificationEntity(
                                        timestamp = System.currentTimeMillis(), message = message,
                                        type = NotificationType.STATION_REMOVED, stationId = lastKnown.id, isRead = false
                                    )
                                    val insertedId = notificationDao.insertNotification(notification)
                                    if (insertedId > 0) {
                                        newNotificationsLogged++
                                        changeMessages.add(message)
                                    }
                                }
                            }
                        }

                        val newStatesToStoreInDb = currentStations.map { it.toLastKnownStateEntity() }
                        notificationDao.updateAllLastKnownStates(newStatesToStoreInDb)

                        if (newNotificationsLogged > 0) {
                            val summary = if (newNotificationsLogged == 1) changeMessages.first() else "$newNotificationsLogged updates detected"
                            showSnackbar(summary, "View")
                        }

                    } catch (e: Exception) {
                        Log.e(TAG_CHECKER, "Error in check loop", e)
                        val isApiError = e is ConnectException || e is UnknownHostException || e is SocketTimeoutException || e is HttpException || e is IllegalStateException
                        val errorPreference = NotificationPreference.API_SYNC_FAILURE

                        if (isApiError) {
                            stationViewModel.setApiReachable(false)
                            val errorMessage = when (e) {
                                is ConnectException, is UnknownHostException, is SocketTimeoutException -> "API Unreachable."
                                is HttpException -> "API Error: ${e.code()}."
                                is IllegalStateException -> "App Error: No station data."
                                else -> "API Error: ${e.localizedMessage ?: "Unknown"}"
                            }
                            if (preferenceManager.isPreferenceEnabled(errorPreference)) {
                                showSnackbar(errorMessage, null)
                                try {
                                    notificationDao.insertNotification(NotificationEntity(timestamp = System.currentTimeMillis(), message = errorMessage, type = NotificationType.SYNC_ERROR, stationId = null))
                                } catch (dbError: Exception) { Log.e(TAG_CHECKER, "DB log failed", dbError) }
                            }
                        } else {
                            if (!apiRefreshSuccess) { stationViewModel.setApiReachable(true) }
                            val errorMessage = "App Error: ${e.localizedMessage ?: "Unknown"}"
                            if (preferenceManager.isPreferenceEnabled(errorPreference)) {
                                showSnackbar(errorMessage, null)
                                try {
                                    notificationDao.insertNotification(NotificationEntity(timestamp = System.currentTimeMillis(), message = "Check error: ${e.message}", type = NotificationType.SYNC_ERROR, stationId = null))
                                } catch (dbError: Exception) { Log.e(TAG_CHECKER, "DB log failed", dbError) }
                            }
                        }
                    }
                    delay(CHECK_INTERVAL_MILLIS)
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private fun constructChangeMessage(current: StationEntity, lastKnown: LastKnownStationStateEntity?, preference: NotificationPreference): String {
        return when(preference) {
            NotificationPreference.STATION_ERROR -> "Station '${current.name}' entered ERROR state (${if(current.state == com.example.team26.domain.station.DeviceState.ERROR) "State" else "Mode"})."
            NotificationPreference.STATION_RECOVERY -> "Station '${current.name}' recovered from ERROR."
            NotificationPreference.STATE_CHANGE -> "Station '${current.name}' state changed to ${current.state}."
            NotificationPreference.MODE_CHANGE -> "Station '${current.name}' mode changed to ${current.controlMode}."
            NotificationPreference.STATION_ADDED_REMOVED -> "Station '${current.name}' detected."
            else -> "Station '${current.name}' updated."
        }
    }

    private fun mapPreferenceToType(preference: NotificationPreference): NotificationType {
        return when(preference) {
            NotificationPreference.STATION_ERROR -> NotificationType.STATE_CHANGE
            NotificationPreference.STATION_RECOVERY -> NotificationType.STATE_CHANGE
            NotificationPreference.API_SYNC_FAILURE -> NotificationType.SYNC_ERROR
            NotificationPreference.STATE_CHANGE -> NotificationType.STATE_CHANGE
            NotificationPreference.MODE_CHANGE -> NotificationType.MODE_CHANGE
            NotificationPreference.STATION_ADDED_REMOVED -> NotificationType.STATION_ADDED
        }
    }
}