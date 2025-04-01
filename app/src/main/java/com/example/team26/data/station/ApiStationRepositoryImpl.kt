package com.example.team26.data.station

import android.util.Log
import com.example.team26.data.converters.StationConverter
import com.example.team26.data.remote.StationApiService
import com.example.team26.domain.station.ControlMode
import com.example.team26.domain.station.StationEntity
import com.example.team26.domain.station.StationRepository
import com.example.team26.domain.station.DeviceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ApiStationRepositoryImpl(private val apiService: StationApiService) : StationRepository {

    private val _stations = MutableStateFlow<List<StationEntity>>(emptyList())
    private val stations: StateFlow<List<StationEntity>> = _stations.asStateFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        repositoryScope.launch {
            refreshStationsInternal()
        }
    }

    private suspend fun refreshStationsInternal(): Result<Unit> {
        return try {
            Log.d("STATION_DEBUG", "Repository: Refreshing stations from API...")
            val apiStations = apiService.getAllDevices()
            val newStations = apiStations.map { StationConverter.toStationEntity(it) }
            _stations.value = newStations
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("STATION_DEBUG", "Repository: Failed to refresh stations", e)

            val isConnectivityError = e is ConnectException ||
                    e is UnknownHostException ||
                    e is SocketTimeoutException ||
                    e is HttpException

            if (isConnectivityError) {
                Log.w("STATION_DEBUG", "API connectivity error during refresh. Clearing station cache.")
                _stations.value = emptyList()
            }

            Result.failure(e)
        }
    }

    override suspend fun refreshStations() {
        withContext(Dispatchers.IO) {
            refreshStationsInternal()
        }
    }

    override fun getAllStations(): Flow<List<StationEntity>> {
        return stations
    }

    override suspend fun getStationById(id: String): Flow<StationEntity?> {
        val cachedStation = stations.value.find { it.id == id }
        if (cachedStation != null) {
            return flowOf(cachedStation)
        }
        return flow {
            try {
                val apiStation = apiService.getDeviceByIdentifier(id)
                val station = StationConverter.toStationEntity(apiStation)
                emit(station)
            } catch (e: Exception) {
                emit(null)
            }
        }
    }

    override suspend fun updateStationState(id: String, state: DeviceState): Result<StationEntity> {
        return try {
            val stateMap = mapOf("state" to state.name.uppercase())
            val postResponse = apiService.setDeviceState(id, stateMap)

            if (postResponse.isSuccessful) {
                val updatedApiStation = apiService.getDeviceByIdentifier(id)
                val updatedStation = StationConverter.toStationEntity(updatedApiStation)
                // Update flow
                val currentList = _stations.value
                _stations.value = currentList.map { if (it.id == updatedStation.id) updatedStation else it }
                Result.success(updatedStation)
            } else {
                val errorBody = postResponse.errorBody()?.string() ?: "Unknown API error"
                Result.failure(Exception("Failed to update state (API Error ${postResponse.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateControlMode(id: String, mode: ControlMode): Result<StationEntity> {
        return try {
            val modeMap = mapOf("astroman" to mode.name.uppercase())
            val postResponse = apiService.setDeviceMode(id, modeMap)

            if (postResponse.isSuccessful) {
                val updatedApiStation = apiService.getDeviceByIdentifier(id)
                val updatedStation = StationConverter.toStationEntity(updatedApiStation)
                // Update flow
                val currentList = _stations.value
                _stations.value = currentList.map { if (it.id == updatedStation.id) updatedStation else it }
                Result.success(updatedStation)
            } else {
                val errorBody = postResponse.errorBody()?.string() ?: "Unknown API error"
                Result.failure(Exception("Failed to update mode (API Error ${postResponse.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}