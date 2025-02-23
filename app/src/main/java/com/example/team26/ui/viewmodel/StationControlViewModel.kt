package com.example.team26.ui.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject // todo: check if this is needed with hilt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.team26.data.repository.StationRepository
import com.example.team26.domain.model.Station
import com.example.team26.domain.model.ControlMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class StationControlViewModel @Inject constructor(
    private val stationRepository: StationRepository
) : ViewModel() {
    private val _stations = MutableStateFlow<List<Station>>(emptyList())
    val stations: StateFlow<List<Station>> = _stations

    init {
        fetchStations()
    }

    private fun fetchStations() {
        viewModelScope.launch {
            stationRepository.getStations().collect { stationList ->
                _stations.value = stationList
            }
        }
    }

    fun updateStationMode(stationId: String, mode: ControlMode) {
        viewModelScope.launch {
            stationRepository.updateStationMode(stationId, mode)
        }
    }

    fun refreshStations() {
        viewModelScope.launch {
            stationRepository.refreshStations()
        }
    }
}