package com.example.team26.data.repository

import com.example.team26.data.local.dao.StationDao
// import com.example.team26.data.models.StationEntity
import com.example.team26.data.remote.api.StationApi
import com.example.team26.data.remote.model.StationDto
import com.example.team26.domain.model.ControlMode
import com.example.team26.domain.model.Station
import com.example.team26.domain.model.StationState
// import com.example.team26.data.remote.model.StateUpdateRequest
// import com.example.team26.data.remote.model.ControlModeUpdateRequest
import com.example.team26.domain.repository.StationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.Response
import javax.inject.Inject

class StationRepositoryImpl @Inject constructor(
      private val api: StationApi,
       private val dao: StationDao,
       // private val dispatchers: DispatcherProvider
   ) : StationRepository {
    override suspend fun getStations(): Flow<List<Station>> {
        TODO("Not yet implemented")
    }
    /*
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
 */

    override suspend fun getStationById(id: String): Flow<Station?> {
        TODO("Not yet implemented")
    }

    override suspend fun updateStationState(id: String, state: StationState): Result<Station> {
        TODO("Not yet implemented")
    }
    /*
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
           }*/

    override suspend fun updateControlMode(id: String, mode: ControlMode): Result<Station> {
        TODO("Not yet implemented")
    }
}

