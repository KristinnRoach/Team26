package com.example.team26.shared.data.remote

// HENDA - mock repo sér um þetta

/*
import com.example.team26.shared.data.station.StationDto


class MockStationApi : StationApi {
    private val stations = mutableListOf<StationDto>()

    init {
        stations.add(
            StationDto(
                id = "station1",
                name = "Station 1",
                latitude = 40.7128,
                longitude = -74.0060,
                currentState = "ON",
                currentMode = "ASTRO",
                timestamp = System.currentTimeMillis()
            )
        )
        stations.add(
            StationDto(
                id = "station2",
                name = "Station 2",
                latitude = 34.0522,
                longitude = -118.2437,
                currentState = "OFF",
                currentMode = "MANUAL",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override suspend fun getAllStations(): List<StationDto> = stations

    override suspend fun getStationById(stationId: String): StationDto =
        stations.find { it.id == stationId } ?: throw Exception("Station not found")

    override suspend fun updateStationState(id: String, state: String): StationDto {
        val station = stations.find { it.id == id } ?: throw Exception("Station not found")
        val updatedStation = station.copy(currentState = state, timestamp = System.currentTimeMillis())
        stations.remove(station)
        stations.add(updatedStation)
        return updatedStation
    }

    override suspend fun updateStationMode(id: String, mode: String): StationDto {
        val station = stations.find { it.id == id } ?: throw Exception("Station not found")
        val updatedStation = station.copy(currentMode = mode, timestamp = System.currentTimeMillis())
        stations.remove(station)
        stations.add(updatedStation)
        return updatedStation
    }
}

 */