package com.example.team26.data.repository

class StationRepositoryImpl @Inject constructor(
    private val api: StationApi,
    private val dao: StationDao,
    private val dispatchers: DispatcherProvider
) : StationRepository {
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
}