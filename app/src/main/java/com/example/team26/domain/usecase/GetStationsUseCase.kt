package com.example.team26.domain.usecase

class GetStationsUseCase @Inject constructor(
    private val repository: StationRepository
) {
    suspend operator fun invoke(): Flow<List<Station>> = repository.getStations()
}
