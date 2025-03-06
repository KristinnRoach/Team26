package com.example.team26.features.station.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.team26.features.station.domain.model.ControlMode
import com.example.team26.features.station.domain.model.Station
import com.example.team26.features.station.domain.model.StationState
import com.example.team26.features.station.domain.repository.StationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * UI state representation for the Station detail screen
 */
data class StationDetailUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val stationId: String = "",
    val stationName: String = "",
    val status: String = "",
    val controlMode: String = "",
    val lastUpdated: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val canToggleState: Boolean = false,
    val canToggleMode: Boolean = false
)

/**
 * ViewModel for displaying and controlling a single station
 */
@HiltViewModel
class StationViewModel @Inject constructor(
    private val repository: StationRepository
) : ViewModel() {

    // Station ID that we're currently viewing
    private val _stationId = MutableStateFlow<String?>(null)

    // Error state
    private val _error = MutableStateFlow<String?>(null)

    // Loading state
    private val _isLoading = MutableStateFlow(false)

    // Combine all states into a single UI state
    val uiState: StateFlow<StationDetailUiState> = repository.getAllStations()
        .map { stations ->
            val stationId = _stationId.value
            val station = if (stationId != null) {
                stations.find { it.id == stationId }
            } else {
                // If no ID is selected, just use the first station
                stations.firstOrNull()
            }

            mapToUiState(station, _isLoading.value, _error.value)
        }
        .catch { error ->
            emit(StationDetailUiState(
                isLoading = false,
                errorMessage = "Error loading station: ${error.message}"
            ))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StationDetailUiState()
        )

    /**
     * Set the station ID to view
     */
    fun setStationId(id: String) {
        _stationId.value = id
    }

    /**
     * Refreshes station data from the repository
     */
    fun refreshStation() {
        Log.d("STATION_DEBUG", "Starting refreshStation, setting isLoading=true")
        _isLoading.value = true

        viewModelScope.launch {
            try {
                repository.refreshStations()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to refresh: ${e.message}"
            } finally {
                Log.d("STATION_DEBUG", "Setting isLoading to false after refreshStation")
                _isLoading.value = false
            }
        }
        _isLoading.value = false // WHY IS THIS NEEDED? If removed gets stuck loading
    }

    /**
     * Toggle the station's power state (ON/OFF)
     */
    fun toggleStationState() {
        val currentStationId = _stationId.value ?: return
        val currentState = uiState.value.status

        // Debug
        Log.d("MyTag", "Toggle state requested: Current ID=$currentStationId, Current state=$currentState")


        viewModelScope.launch {
            try {
                _isLoading.value = true
                val newState = if (currentState == "ON") StationState.OFF else StationState.ON
                repository.updateStationState(currentStationId, newState)
                _error.value = null

                // Debug
                Log.d("MyTag", "Toggle state completed: New state=${newState.name}")
            } catch (e: Exception) {
                _error.value = "Failed to update state: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Toggle the station's control mode (ASTRO/MANUAL)
     */
    fun toggleControlMode() {
        val currentStationId = _stationId.value ?: return
        val currentMode = uiState.value.controlMode

        // Debug
        Log.d("MyTag", "Toggle mode requested: Current ID=$currentStationId, Current mode=$currentMode")

        val newMode = if (currentMode == "ASTRO") ControlMode.MANUAL else ControlMode.ASTRO

        viewModelScope.launch {
            try {
                _isLoading.value = true

                repository.updateControlMode(currentStationId, newMode)
                _error.value = null

                // Debug
                Log.d("MyTag", "Toggle mode completed: New mode=${newMode.name}")
            } catch (e: Exception) {
                _error.value = "Failed to update mode: ${e.message}"

                // Debug
                Log.e("MyTag", "Toggle mode failed: ${e.message}")
            } finally {
                Log.d("STATION_DEBUG", "Setting isLoading to false after control mode toggle")

                _isLoading.value = false
            }
        }
    }

    /**
     * Maps domain model to UI state
     */
    private fun mapToUiState(
        station: Station?,
        isLoading: Boolean,
        errorMessage: String?
    ): StationDetailUiState {
        if (station == null) {
            return StationDetailUiState(
                isLoading = isLoading,
                errorMessage = errorMessage ?: "Station not found"
            )
        }

        // Format date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(station.lastUpdated))

        return StationDetailUiState(
            isLoading = isLoading,
            errorMessage = errorMessage,
            stationId = station.id,
            stationName = station.name,
            status = station.state.name,
            controlMode = station.controlMode.name,
            lastUpdated = formattedDate,
            latitude = station.location.latitude.toString(),
            longitude = station.location.longitude.toString(),
            canToggleState = station.state != StationState.ERROR,
            canToggleMode = station.controlMode != ControlMode.ERROR
        )
    }
}

/*
BEFORE MOCKING: gamalt fyrir neðan - Henda þegar komið í gagnið

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

 */