package com.example.team26.data.station

import android.util.Log
import com.example.team26.domain.station.ControlMode
import com.example.team26.domain.station.Location
import com.example.team26.domain.station.StationEntity
import com.example.team26.domain.station.StationRepository
import com.example.team26.domain.station.DeviceState
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

    override suspend fun refreshStations(): Result<Unit> { // returns Unit (implicit)
        // Simulate network delay
        delay(800)

        val updatedStations = _stations.value.map {
            it.copy(lastUpdated = System.currentTimeMillis())
        }

        // Update the flow with new list directly
        _stations.value = updatedStations

        Log.d("STATION_DEBUG", "Repository: refreshStations called, updating ${stations.value.size} stations")

        return Result.success(Unit)
    }

    // Simplified update method:
    override suspend fun updateStationState(id: String, state: DeviceState): Result<StationEntity> {
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
                location = Location(64.1466, -21.9426), // Reykjavik center
                state = DeviceState.ON,
                controlMode = ControlMode.ASTRO,
                lastUpdated = Date().time - 3600000 // 1 hour ago
            ),
            StationEntity(
                id = "station-2",
                name = "Harbor View",
                location = Location(64.1509, -21.9539), // Near harbor
                state = DeviceState.OFF,
                controlMode = ControlMode.MANUAL,
                lastUpdated = Date().time - 7200000 // 2 hours ago
            ),
            StationEntity(
                id = "station-3",
                name = "University Square",
                location = Location(64.1397, -21.9558), // University area
                state = DeviceState.ON,
                controlMode = ControlMode.ASTRO,
                lastUpdated = Date().time
            ),
            StationEntity(
                id = "station-4",
                name = "Old Town East",
                location = Location(64.1474, -21.9337), // East side
                state = DeviceState.ERROR,
                controlMode = ControlMode.ERROR,
                lastUpdated = Date().time - 86400000 // 24 hours ago
            ),
            StationEntity(
                id = "station-5",
                name = "Viðey Island",
                location = Location(64.1646, -21.8730), // Island location
                state = DeviceState.OFF,
                controlMode = ControlMode.MANUAL,
                lastUpdated = Date().time - 1800000 // 30 minutes ago
            ),
            StationEntity(
                id = "station-6",
                name = "Northern Lights Park",
                location = Location(64.1832, -21.9124), // Northern outskirts
                state = DeviceState.ON,
                controlMode = ControlMode.MANUAL,
                lastUpdated = Date().time - 300000 // 5 minutes ago
            ),
            StationEntity(
                id = "station-7",
                name = "South Bridge Terminal",
                location = Location(64.1289, -21.8967), // Southern area
                state = DeviceState.ON,
                controlMode = ControlMode.ASTRO,
                lastUpdated = Date().time
            ),
            StationEntity(
                id = "station-8",
                name = "West Market Plaza",
                location = Location(64.1423, -22.0015), // Western district
                state = DeviceState.OFF,
                controlMode = ControlMode.MANUAL,
                lastUpdated = Date().time - 10800000 // 3 hours ago
            ),
            StationEntity(
                id = "station-9",
                name = "Hilltop Observatory",
                location = Location(64.1711, -21.9456), // Higher elevation
                state = DeviceState.ERROR,
                controlMode = ControlMode.MANUAL,
                lastUpdated = Date().time - 43200000 // 12 hours ago
            ),
            StationEntity(
                id = "station-10",
                name = "Coastal Walkway",
                location = Location(64.1587, -21.9873), // Coastline
                state = DeviceState.ON,
                controlMode = ControlMode.MANUAL,
                lastUpdated = Date().time - 600000 // 10 minutes ago
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

