package com.example.team26.domain.usecase

class UpdateControlModeUseCase @Inject constructor(
    private val repository: StationRepository
) {
    suspend operator fun invoke(id: String, mode: ControlMode): Result<Station> =
        repository.updateControlMode(id, mode)
}