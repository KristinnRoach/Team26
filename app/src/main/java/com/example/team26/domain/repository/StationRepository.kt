package com.example.team26.domain.repository

import com.example.team26.domain.model.ControlMode
import com.example.team26.domain.model.Station
import com.example.team26.domain.model.StationState
import kotlinx.coroutines.flow.Flow

interface StationRepository {
    fun getAllStations(): Flow<List<Station>> // suspend ?
    suspend fun getStationById(id: String): Flow<Station?>
    suspend fun refreshStations(): Void
    suspend fun updateStationState(id: String, state: StationState): Result<Station>
    suspend fun updateControlMode(id: String, mode: ControlMode): Result<Station>
}