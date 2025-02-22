package com.example.team26.domain.usecase

class UpdateStationStateUseCase @Inject constructor(
    private val repository: StationRepository
) {
    suspend operator fun invoke(id: String, state: StationState): Result<Station> =
        repository.updateStationState(id, state)
}