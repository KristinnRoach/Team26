package com.example.team26.domain.notification

import com.example.team26.domain.station.StationEntity

data class StateChangeInfo(
    val message: String,
    val type: NotificationType,
    val stationId: String?
)

object NotificationComparator {

    fun compareStates(
        currentStations: List<StationEntity>,
        lastKnownStates: List<LastKnownStationStateEntity>
    ): List<StateChangeInfo> {
        val changes = mutableListOf<StateChangeInfo>()
        val lastKnownMap = lastKnownStates.associateBy { it.id }
        val currentMap = currentStations.associateBy { it.id }

        for (current in currentStations) {
            val lastKnown = lastKnownMap[current.id]
            if (lastKnown == null) {
                changes.add(
                    StateChangeInfo(
                        "Station '${current.name}' detected.",
                        NotificationType.STATION_ADDED, current.id
                    )
                )
            } else {
                if (current.state != lastKnown.state) {
                    changes.add(
                        StateChangeInfo(
                            "Station '${current.name}' state changed to ${current.state}.",
                            NotificationType.STATE_CHANGE, current.id
                        )
                    )
                }
                if (current.controlMode != lastKnown.controlMode) {
                    changes.add(
                        StateChangeInfo(
                            "Station '${current.name}' mode changed to ${current.controlMode}.",
                            NotificationType.MODE_CHANGE, current.id
                        )
                    )
                }
                if (current.name != lastKnown.name) {
                    changes.add(StateChangeInfo("Station ID '${current.id}' name changed to '${current.name}'.", NotificationType.INFO, current.id))
                }
            }
        }

        // Check removed stations
        for (lastKnown in lastKnownStates) {
            if (!currentMap.containsKey(lastKnown.id)) {
                changes.add(
                    StateChangeInfo(
                        "Station '${lastKnown.name}' may be offline or removed.",
                        NotificationType.STATION_REMOVED, lastKnown.id
                    )
                )
            }
        }
        return changes
    }

    fun StationEntity.toLastKnownStateEntity(): LastKnownStationStateEntity = LastKnownStationStateEntity(
        id = this.id,
        name = this.name,
        state = this.state,
        controlMode = this.controlMode,
        lastCheckedTimestamp = System.currentTimeMillis()
    )
}