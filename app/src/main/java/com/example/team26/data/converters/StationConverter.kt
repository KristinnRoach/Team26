package com.example.team26.data.converters

import com.example.team26.domain.station.ApiStationEntity
import com.example.team26.domain.station.ControlMode
import com.example.team26.domain.station.DeviceState
import com.example.team26.domain.station.Location
import com.example.team26.domain.station.StationEntity
import java.time.Instant
import java.time.format.DateTimeFormatter

object StationConverter {
    fun toStationEntity(apiEntity: ApiStationEntity): StationEntity {
        val stationId = apiEntity.id ?: throw IllegalArgumentException("API returned station data with a null ID.")

        return StationEntity(
            id = stationId,
            name = apiEntity.hs ?: "Unknown",
            location = Location(
                latitude = apiEntity.lat ?: 0.0,
                longitude = apiEntity.lon ?: 0.0
            ),
            state = when (apiEntity.uxstate?.uppercase()) {
                "ON" -> DeviceState.ON
                "OFF" -> DeviceState.OFF
                else -> DeviceState.ERROR
            },
            controlMode = when (apiEntity.astroman?.uppercase()) {
                "ASTRO" -> ControlMode.ASTRO
                "MANUAL" -> ControlMode.MANUAL
                else -> ControlMode.ERROR
            },
            lastUpdated = apiEntity.updated_at?.let {
                try {
                    DateTimeFormatter.ISO_DATE_TIME
                        .parse(it)
                        .let { Instant.from(it).toEpochMilli() }
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }
            } ?: System.currentTimeMillis()
        )
    }
}