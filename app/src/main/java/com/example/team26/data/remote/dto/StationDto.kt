package com.example.team26.data.remote.dto

import com.example.team26.domain.model.ControlMode
import com.example.team26.domain.model.Location
import com.example.team26.domain.model.Station
import com.example.team26.domain.model.StationState

// import kotlinx.serialization.Serializable

//@Serializable
data class StationDto(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val currentState: String,
    val currentMode: String,
    val timestamp: Long
) {
    fun toDomain(): Station = Station(
        id = id,
        name = name,
        location = Location(latitude, longitude),
        state = when (currentState.uppercase()) {
            "ON" -> StationState.ON
            "OFF" -> StationState.OFF
            else -> StationState.ERROR
        },
        controlMode = when (currentMode.uppercase()) {
            "AUTO" -> ControlMode.AUTO
            "MANUAL" -> ControlMode.MANUAL
            else -> ControlMode.ERROR
        },
        lastUpdated = timestamp
    )
}