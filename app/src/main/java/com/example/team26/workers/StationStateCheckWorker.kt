package com.example.team26.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
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
    private val systemNotificationManager = SystemNotificationManager(appContext)

    companion object {
        const val WORK_NAME = "StationStateCheckWorker"
        private const val TAG = "StationStateCheckWorker"
    }

    override suspend fun doWork(): Result {
        var currentApiStations: List<StationEntity>? = null
        var lastKnownStates: List<LastKnownStationStateEntity> = emptyList()
        val lastKnownStateMap: Map<String, LastKnownStationStateEntity>

        try {
            stationRepository.refreshStations()
            currentApiStations = stationRepository.getAllStations().first()

            lastKnownStates = notificationDao.getAllLastKnownStates()
            lastKnownStateMap = lastKnownStates.associateBy { it.id }

            if (currentApiStations == null) {
                handleOtherError(Exception("No station data."), "Internal Error: No station data.")
                return Result.failure()
            }

            val currentStationMap = currentApiStations.associateBy { it.id }
            var newNotificationCount = 0
            val notificationMessages = mutableListOf<String>()

            currentApiStations.forEach { currentStation ->
                val lastKnown = lastKnownStateMap[currentStation.id]
                val preference = NotificationMapper.mapComparisonToPreference(currentStation, lastKnown)

                if (preference != null && preferenceManager.isPreferenceEnabled(preference)) {
                    val message = constructChangeMessage(currentStation, lastKnown, preference)
                    val notification = NotificationEntity(
                        timestamp = System.currentTimeMillis(),
                        message = message,
                        type = mapPreferenceToType(preference),
                        stationId = currentStation.id,
                        isRead = false
                    )
                    val insertedId = notificationDao.insertNotification(notification)
                    if (insertedId > 0) {
                        newNotificationCount++
                        notificationMessages.add(message)
                    }
                }
            }

            if (preferenceManager.isPreferenceEnabled(NotificationPreference.STATION_ADDED_REMOVED)) {
                lastKnownStates.forEach { lastKnown ->
                    if (!currentStationMap.containsKey(lastKnown.id)) {
                        val message = "Station '${lastKnown.name}' may be offline or removed."
                        val notification = NotificationEntity(
                            timestamp = System.currentTimeMillis(),
                            message = message,
                            type = NotificationType.STATION_REMOVED,
                            stationId = lastKnown.id,
                            isRead = false
                        )
                        val insertedId = notificationDao.insertNotification(notification)
                        if (insertedId > 0) {
                            newNotificationCount++
                            notificationMessages.add(message)
                        }
                    }
                }
            }

            if (newNotificationCount > 0) {
                systemNotificationManager.showSummaryNotification(
                    title = "Station Updates",
                    summaryText = "$newNotificationCount station(s) updated.",
                    details = notificationMessages
                )
            }

            val newStatesToStore = currentApiStations.map { it.toLastKnownStateEntity() }
            notificationDao.updateAllLastKnownStates(newStatesToStore)

            return Result.success()

        } catch (e: ConnectException) { return handleApiError(e, "API Unreachable.") }
        catch (e: UnknownHostException) { return handleApiError(e, "DNS Error.") }
        catch (e: SocketTimeoutException) { return handleApiError(e, "Timeout.") }
        catch (e: HttpException) { return handleApiError(e, "API Error: ${e.code()}.") }
        catch (e: Exception) { return handleOtherError(e, "Check failed: ${e.message ?: "Unknown"}") }
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

    private suspend fun handleApiError(e: Exception, userMessage: String): Result {
        val preference = NotificationPreference.API_SYNC_FAILURE
        if (preferenceManager.isPreferenceEnabled(preference)) {
            try {
                notificationDao.insertNotification(
                    NotificationEntity(
                        timestamp = System.currentTimeMillis(), message = userMessage,
                        type = NotificationType.SYNC_ERROR, stationId = null
                    )
                )
                systemNotificationManager.showSummaryNotification(
                    title = "API Issue",
                    summaryText = userMessage,
                    details = listOf("Could not connect.", "Will retry.")
                )
            } catch (dbError: Exception) { Log.e(TAG, "Log failed", dbError) }
        }
        return Result.retry()
    }

    private suspend fun handleOtherError(e: Exception, userMessage: String): Result {
        val preference = NotificationPreference.API_SYNC_FAILURE
        if (preferenceManager.isPreferenceEnabled(preference)) {
            try {
                notificationDao.insertNotification(NotificationEntity(timestamp = System.currentTimeMillis(), message = userMessage, type = NotificationType.SYNC_ERROR, stationId = null))
            } catch (dbError: Exception) { Log.e(TAG, "Log failed", dbError) }
        }
        return Result.failure()
    }
}