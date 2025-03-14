package com.example.team26.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.team26.domain.auth.AuthRepository
import com.example.team26.domain.station.StationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import com.example.team26.domain.station.StationEntity
import kotlinx.coroutines.flow.StateFlow


class MainViewModel(
    private val stationRepository: StationRepository,
    private val authRepository: AuthRepository
): ViewModel() {

    // Use if the selected station needs to be used outside the StationViewModel
    private val _selectedStation = MutableStateFlow<StationEntity?>(null)
    val selectedStation: StateFlow<StationEntity?> = _selectedStation

    fun selectStation(station: StationEntity) {
        _selectedStation.value = station
    }

    fun clearSelectedStation() {
        _selectedStation.value = null
    }

    init {
        viewModelScope.launch {
            stationRepository.getAllStations()
        }
    }

    fun login() {
        viewModelScope.launch {
            authRepository.login("test@test.com", "test123")
        }
    }
}