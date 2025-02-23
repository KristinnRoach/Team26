package com.example.team26.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.team26.domain.model.Station
import com.example.team26.domain.model.Location
import com.example.team26.domain.model.StationState
import com.example.team26.domain.model.ControlMode

@Entity(tableName = "stations")
data class StationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val state: StationState,
    val controlMode: ControlMode,
    val lastUpdated: Long
) {
    fun toDomain(): Station = Station(
        id = id,
        name = name,
        location = Location(latitude, longitude),
        state = state,
        controlMode = controlMode,
        lastUpdated = lastUpdated
    )

    companion object {
        fun fromDomain(station: Station): StationEntity = StationEntity(
            id = station.id,
            name = station.name,
            latitude = station.location.latitude,
            longitude = station.location.longitude,
            state = station.state,
            controlMode = station.controlMode,
            lastUpdated = station.lastUpdated
        )
    }
}
