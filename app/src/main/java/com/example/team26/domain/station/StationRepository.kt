package com.example.team26.domain.station

import kotlinx.coroutines.flow.Flow

interface StationRepository {
    fun getAllStations(): Flow<List<StationEntity>> // suspend ?
    suspend fun getStationById(id: String): Flow<StationEntity?>
    suspend fun refreshStations(): Result<Unit>
    suspend fun updateStationState(id: String, state: DeviceState): Result<StationEntity>
    suspend fun updateControlMode(id: String, mode: ControlMode): Result<StationEntity>
}