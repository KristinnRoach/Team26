package com.example.team26.data.station

import com.example.team26.domain.station.ControlMode
import com.example.team26.domain.station.Location
import com.example.team26.domain.station.StationEntity
import com.example.team26.domain.station.StationState

/**
 * Data Transfer Object for Station data from the API
 */
data class StationDto(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val currentState: String,  // "ON" or "OFF"
    val currentMode: String,   // "ASTRO" or "MANUAL"
    val timestamp: Long
) {
    /**
     * Maps the DTO to the domain model
     */
    fun toDomain(): StationEntity = StationEntity(
        id = id,
        name = name,
        location = Location(latitude, longitude),
        state = when (currentState.uppercase()) {
            "ON" -> StationState.ON
            "OFF" -> StationState.OFF
            else -> StationState.ERROR
        },
        controlMode = when (currentMode.uppercase()) {
            "ASTRO" -> ControlMode.ASTRO
            "MANUAL" -> ControlMode.MANUAL
            else -> ControlMode.ERROR
        },
        lastUpdated = timestamp
    )
}

/**
 * DTO for sending state update requests
 */
data class StateUpdateRequest(
    val state: String  // Should be "ON" or "OFF"
)

/**
 * DTO for sending control mode update requests
 */
data class ControlModeUpdateRequest(
    val astroman: String  // Should be "ASTRO" or "MANUAL"
)
