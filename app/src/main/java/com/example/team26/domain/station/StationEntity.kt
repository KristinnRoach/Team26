package com.example.team26.domain.station

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "stations",
    indices = [
        Index(value = ["state"]),
        Index(value = ["controlMode"])
    ]
)data class StationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val location: Location,
    val state: DeviceState,
    val controlMode: ControlMode,
    val lastUpdated: Long
)

data class Location(
    val latitude: Double,
    val longitude: Double
)

enum class DeviceState {
    ON, OFF, ERROR
}

enum class ControlMode {
    ASTRO, MANUAL, ERROR
}