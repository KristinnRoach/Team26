package com.example.team26.data.station

import android.util.Log
import com.example.team26.domain.station.ControlMode
import com.example.team26.domain.station.Location
import com.example.team26.domain.station.StationEntity
import com.example.team26.domain.station.StationRepository
import com.example.team26.domain.station.StationState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import java.util.Date

/**
 * A mock implementation of StationRepository for development and testing.
 * This class simulates API responses and provides local caching until the real API is available.
 */
class MockStationRepositoryImpl: StationRepository {

    // In-memory cache of stations
    private val _stations = MutableStateFlow<List<StationEntity>>(emptyList())
    private val stations = _stations.asStateFlow()

    init {
        // Initialize with mock data
        _stations.value = createMockStations()

        Log.d("STATION_DEBUG", "Repository: Initialized with ${stations.value.size} stations")
    }

    override fun getAllStations(): Flow<List<StationEntity>> {
        Log.d("STATION_DEBUG", "Repository: getAllStations called, returning ${stations.value.size} stations")

        return stations
    }

    override suspend fun getStationById(id: String): Flow<StationEntity?> {
        // Simulate network delay
        delay(300)
        val station = stations.value.find { it.id == id }

        Log.d("STATION_DEBUG", "Repository: getStationById called for id $id, returning ${station?.name ?: "null"}")
        return flowOf(station)
    }

    override suspend fun refreshStations() { // returns Unit (implicit)
        // Simulate network delay
        delay(800)

        val updatedStations = _stations.value.map {
            it.copy(lastUpdated = System.currentTimeMillis())
        }

        // Update the flow with new list directly
        _stations.value = updatedStations

        Log.d("STATION_DEBUG", "Repository: refreshStations called, updating ${stations.value.size} stations")
    }

    // Simplified update method:
    override suspend fun updateStationState(id: String, state: StationState): Result<StationEntity> {
        val updatedStations = _stations.value.map { station ->
            if (station.id == id) {
                station.copy(
                    state = state,
                    lastUpdated = System.currentTimeMillis()
                )
            } else {
                station
            }
        }

        // Update the flow with new list
        _stations.value = updatedStations

        Log.d("STATION_DEBUG", "Repository: updateStationState called for id $id, updating state to $state")

        // Return the updated station or an error
        return updatedStations.find { it.id == id }?.let {
            Result.success(it)
        } ?: Result.failure(Exception("Station not found"))
    }

    override suspend fun updateControlMode(id: String, mode: ControlMode): Result<StationEntity> {
        // Using the same pattern as updateStationState
        val updatedStations = _stations.value.map { station ->
            if (station.id == id) {
                station.copy(
                    controlMode = mode,
                    lastUpdated = System.currentTimeMillis()
                )
            } else {
                station
            }
        }

        // Update the flow with new list
        _stations.value = updatedStations

        Log.d("STATION_DEBUG", "Repository: updateControlMode called for id $id, updating control mode to $mode")

        // Return the updated station or an error
        return updatedStations.find { it.id == id }?.let {
            Result.success(it)
        } ?: Result.failure(Exception("Station not found"))
    }

    private fun createMockStations(): List<StationEntity> {

        Log.d("STATION_DEBUG", "Repository: createMockStations called")

        return listOf(
            StationEntity(
                id = "station-1",
                name = "Downtown Main Street",
                location = Location(64.1466, -21.9426),
                state = StationState.ON,
                controlMode = ControlMode.ASTRO,
                lastUpdated = Date().time
            ),
            StationEntity(
                id = "station-2",
                name = "Harbor View",
                location = Location(64.1509, -21.9539),
                state = StationState.OFF,
                controlMode = ControlMode.MANUAL,
                lastUpdated = Date().time
            ),
            StationEntity(
                id = "station-3",
                name = "University Square",
                location = Location(64.1397, -21.9558),
                state = StationState.ON,
                controlMode = ControlMode.ASTRO,
                lastUpdated = Date().time
            ),
            StationEntity(
                id = "station-4",
                name = "Old Town East",
                location = Location(64.1474, -21.9337),
                state = StationState.ERROR,
                controlMode = ControlMode.ERROR,
                lastUpdated = Date().time
            ),
            StationEntity(
                id = "station-5",
                name = "Viðey Island",
                location = Location(64.1646, -21.8730),
                state = StationState.OFF,
                controlMode = ControlMode.MANUAL,
                lastUpdated = Date().time
            )
        )
    }
/*
    companion object {
        class MockStationApi : StationApi {
            constructor() {
                Log.d("STATION_DEBUG", "MockStationApi: Constructor called")
            }
        }
    }

 */
}

