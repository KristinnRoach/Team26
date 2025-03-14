package com.example.team26.domain.station

import kotlinx.coroutines.flow.Flow

interface StationRepository {
    fun getAllStations(): Flow<List<StationEntity>> // suspend ?
    suspend fun getStationById(id: String): Flow<StationEntity?>
    suspend fun refreshStations() // Changed return type from Void to Unit (implicit)
    suspend fun updateStationState(id: String, state: DeviceState): Result<StationEntity>
    suspend fun updateControlMode(id: String, mode: ControlMode): Result<StationEntity>
}