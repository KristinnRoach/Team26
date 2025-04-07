package com.example.team26.domain.notification

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.team26.domain.station.ControlMode
import com.example.team26.domain.station.DeviceState

@Entity(tableName = "last_known_station_states")
data class LastKnownStationStateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val state: DeviceState,
    val controlMode: ControlMode,
    val lastCheckedTimestamp: Long // When this state was recorded
)