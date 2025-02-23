package com.example.team26.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "stations")
data class Station(
    @PrimaryKey val id: String,
    val name: String,
    val location: Location,
    val state: StationState,
    val controlMode: ControlMode,
    val lastUpdated: Long
)

data class Location(
    val latitude: Double,
    val longitude: Double
)

enum class StationState {
    ON, OFF, ERROR
}

enum class ControlMode {
    AUTO, MANUAL, ERROR
}