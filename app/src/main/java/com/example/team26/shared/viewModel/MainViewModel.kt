package com.example.team26.shared.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.team26.shared.domain.auth.AuthRepository
import com.example.team26.shared.domain.station.StationRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val stationRepository: StationRepository,
    private val authRepository: AuthRepository
): ViewModel() {

    fun login() {
        viewModelScope.launch {
            authRepository.login("test@test.com", "test123")
        }
    }
}