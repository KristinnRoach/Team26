package com.example.team26.domain.streetLamp

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "stations",
    indices = [
        Index(value = ["state"]),
        Index(value = ["controlMode"])
    ]
)data class StreetLampEntity(
    @PrimaryKey val id: String,
    val location: Location,
    val state: StreetLampState,
    val controlMode: ControlMode,
    val lastUpdated: Long
)

data class Location(
    val latitude: Double,
    val longitude: Double
)

enum class StreetLampState {
    ON, OFF, ERROR
}

enum class ControlMode {
    ASTRO, MANUAL, ERROR
}