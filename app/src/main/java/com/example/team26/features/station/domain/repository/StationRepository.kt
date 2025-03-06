package com.example.team26.features.station.domain.repository

import com.example.team26.features.station.domain.model.ControlMode
import com.example.team26.features.station.domain.model.Station
import com.example.team26.features.station.domain.model.StationState
import kotlinx.coroutines.flow.Flow

interface StationRepository {
    fun getAllStations(): Flow<List<Station>> // suspend ?
    suspend fun getStationById(id: String): Flow<Station?>
    suspend fun refreshStations() // Changed return type from Void to Unit (implicit)
    suspend fun updateStationState(id: String, state: StationState): Result<Station>
    suspend fun updateControlMode(id: String, mode: ControlMode): Result<Station>
}