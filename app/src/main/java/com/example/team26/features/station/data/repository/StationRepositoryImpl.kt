package com.example.team26.features.station.data.repository

import com.example.team26.features.station.data.local.dao.StationDao
import com.example.team26.features.station.data.local.entity.StationEntity
import com.example.team26.features.station.data.remote.api.StationApi
import com.example.team26.features.station.domain.model.ControlMode
import com.example.team26.features.station.domain.model.Station
// import com.example.team26.data.remote.model.StateUpdateRequest
// import com.example.team26.data.remote.model.ControlModeUpdateRequest
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@ViewModelScoped
class StationRepository @Inject constructor(
    private val stationDao: StationDao,
    private val stationApi: StationApi
) {
    // Todo: Implement Interface (override methods)
    fun getStations(): Flow<List<Station>> =
        stationDao.getAllStations().map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun refreshStations() {
        val stationsFromApi = stationApi.getAllStations()
        val stationEntities = stationsFromApi.map {
            StationEntity.fromDomain(it.toDomain())
        }
        stationDao.insertAll(stationEntities)
    }

    suspend fun updateStationMode(stationId: String, mode: ControlMode): Station {
        val updatedStationDto = stationApi.updateStationMode(stationId, mode.name)
        val updatedStation = updatedStationDto.toDomain()
        val updatedStationEntity = StationEntity.fromDomain(updatedStation)
        stationDao.insertStation(updatedStationEntity)
        return updatedStation
    }
}

/*
class StationRepositoryImpl @Inject constructor(
      private val api: StationApi,
       private val dao: StationDao,
       // private val dispatchers: DispatcherProvider
   ) : StationRepository {
    override suspend fun getStations(): Flow<List<Station>> {
        TODO("Not yet implemented")
    }

    override suspend fun getStations(): Flow<List<Station>> = flow {
        // Emit cached data first
        emitAll(dao.getStations().map { it.toDomain() })

        // Fetch fresh data from API
        try {
            val response = api.getStations()
            if (response.isSuccessful) {
                response.body()?.let { stations ->
                    dao.insertStations(stations.toEntity())
                }
            }
        } catch (e: Exception) {
            // Error handling based on your requirements
        }
    }.flowOn(dispatchers.io)


    override suspend fun getStationById(id: String): Flow<Station?> {
        TODO("Not yet implemented")
    }

    override suspend fun updateStationState(id: String, state: StationState): Result<Station> {
        TODO("Not yet implemented")
    }

        override suspend fun updateStationState(
               id: String,
               state: StationState
           ): Result<Station> = withContext(dispatchers.io) {
               try {
                   val response = api.updateStationState(
                       id,
                       StateUpdateRequest(state.name)
                   )
                   if (response.isSuccessful) {
                       response.body()?.let { dto ->
                           val entity = dto.toEntity()
                           dao.updateStation(entity)
                           Result.success(entity.toDomain())
                       } ?: Result.failure(Exception("Empty response"))
                   } else {
                       Result.failure(Exception("API error: ${response.code()}"))
                   }
               } catch (e: Exception) {
                   Result.failure(e)
               }
           }

    override suspend fun updateControlMode(id: String, mode: ControlMode): Result<Station> {
        TODO("Not yet implemented")
    }
}

 */

