package com.example.team26.domain.notification

import com.example.team26.domain.station.ControlMode
import com.example.team26.domain.station.DeviceState
import com.example.team26.domain.station.StationEntity

object NotificationMapper {

    fun mapComparisonToPreference(
        current: StationEntity,
        lastKnown: LastKnownStationStateEntity?
    ): NotificationPreference? {
        if (lastKnown == null) {
            return NotificationPreference.STATION_ADDED_REMOVED
        }

        val stateChanged = current.state != lastKnown.state
        val modeChanged = current.controlMode != lastKnown.controlMode

        val enteredErrorState = (stateChanged && current.state == DeviceState.ERROR) ||
                (modeChanged && current.controlMode == ControlMode.ERROR)
        val recoveredFromErrorState = (stateChanged && lastKnown.state == DeviceState.ERROR && current.state != DeviceState.ERROR) ||
                (modeChanged && lastKnown.controlMode == ControlMode.ERROR && current.controlMode != ControlMode.ERROR)

        return when {
            enteredErrorState -> NotificationPreference.STATION_ERROR
            recoveredFromErrorState -> NotificationPreference.STATION_RECOVERY
            stateChanged -> NotificationPreference.STATE_CHANGE
            modeChanged -> NotificationPreference.MODE_CHANGE
            else -> null
        }
    }

    // Map notification type to preference
    fun mapTypeToPreference(type: NotificationType): NotificationPreference? {
        return when(type) {
            NotificationType.SYNC_ERROR -> NotificationPreference.API_SYNC_FAILURE
            NotificationType.STATION_ADDED -> NotificationPreference.STATION_ADDED_REMOVED
            NotificationType.STATION_REMOVED -> NotificationPreference.STATION_ADDED_REMOVED
            else -> null
        }
    }
}