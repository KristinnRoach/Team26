package com.example.team26.features.station.ui

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject // todo: check if this is needed with hilt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.team26.features.station.data.repository.StationRepository
import com.example.team26.features.station.domain.model.Station
import com.example.team26.features.station.domain.model.ControlMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class StationViewModel @Inject constructor(
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