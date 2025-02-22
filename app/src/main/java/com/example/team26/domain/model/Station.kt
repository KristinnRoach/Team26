package com.example.team26.domain.model

data class Station(
    val id: String,
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