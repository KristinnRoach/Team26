package com.example.team26.data.remote.model

// import kotlinx.serialization.Serializable

//@Serializable
data class StationDto(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val state: String,
    val controlMode: String,
    val lastUpdated: Long
)