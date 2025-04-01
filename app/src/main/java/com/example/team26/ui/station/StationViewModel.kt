package com.example.team26.ui.station

import android.net.http.HttpException
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.team26.domain.notification.NotificationComparator.toLastKnownStateEntity
import com.example.team26.domain.notification.NotificationDao
import com.example.team26.domain.station.ControlMode
import com.example.team26.domain.station.StationEntity
import com.example.team26.domain.station.DeviceState
import com.example.team26.domain.station.StationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

class StationViewModel(
    private val repository: StationRepository,
    private val notificationDao: NotificationDao
) : ViewModel() {

    private val _stationId = MutableStateFlow<String?>(null)
    val selectedStationId: StateFlow<String?> = _stationId

    private val _error = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isApiReachable = MutableStateFlow(true)
    val isApiReachable: StateFlow<Boolean> = _isApiReachable.asStateFlow()

    val uiState: StateFlow<StationDetailUiState> = combine(
        _stationId,
        repository.getAllStations(),
        _isLoading,
        _error,
        _isApiReachable
    ) { stationId, stations, isLoading, errorMsg, isApiReachable ->
        val station = stationId?.let { id -> stations.find { it.id == id } }
        val combinedErrorMsg = if (!isApiReachable) "API unreachable. Using cached data." else errorMsg
        mapToUiState(station, isLoading, combinedErrorMsg, isApiReachable)
    }
        .catch { error ->
            val finalErrorMsg = if (!_isApiReachable.value) "API unreachable." else "Failed to load: ${error.message}"
            emit(StationDetailUiState(isLoading = false, stationId = _stationId.value ?: "", errorMessage = finalErrorMsg))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StationDetailUiState(isLoading = true)
        )

    val allStationsUiState: StateFlow<List<StationDetailUiState>> = repository.getAllStations()
        .map { stations ->
            stations.map { station -> mapToUiState(station, false, null, _isApiReachable.value) }
        }
        .catch { error ->
            emit(listOf(StationDetailUiState(isLoading = false, errorMessage = "Error loading list: ${error.message}")))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setApiReachable(reachable: Boolean) {
        if (_isApiReachable.value != reachable) {
            _isApiReachable.value = reachable
        }
    }

    fun setStationId(id: String?) {
        if (_stationId.value != id) {
            _stationId.value = id
            _error.value = null
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun refreshStation() {
        val idToRefresh = _stationId.value
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                repository.refreshStations()
                setApiReachable(true)
            } catch (e: Exception) {
                val isConnectivityError = e is ConnectException || e is UnknownHostException || e is SocketTimeoutException || e is HttpException
                if (isConnectivityError) {
                    setApiReachable(false)
                    _error.value = "API Unreachable"
                } else {
                    setApiReachable(true)
                    _error.value = "Refresh failed: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun toggleStationState() {
        val currentStationId = _stationId.value ?: return
        if (!_isApiReachable.value) {
            _error.value = "API unreachable."
            return
        }

        val currentState = DeviceState.entries.find { it.name == uiState.value.status } ?: DeviceState.ERROR
        if (currentState == DeviceState.ERROR || !uiState.value.canToggleState) {
            _error.value = "Cannot toggle state now."
            return
        }

        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val newState = if (currentState == DeviceState.ON) DeviceState.OFF else DeviceState.ON
                val result = repository.updateStationState(currentStationId, newState)
                result.onSuccess { updatedStation ->
                    setApiReachable(true)
                    try {
                        notificationDao.insertLastKnownStates(listOf(updatedStation.toLastKnownStateEntity()))
                    } catch (dbException: Exception) {
                        Log.e("StationViewModel", "DB update failed", dbException)
                    }
                }.onFailure { e ->
                    val isConnectivityError = e is ConnectException || e is UnknownHostException || e is SocketTimeoutException || e is HttpException || e.cause is ConnectException
                    if (isConnectivityError) {
                        setApiReachable(false)
                        _error.value = "API Unreachable."
                    } else {
                        setApiReachable(true)
                        _error.value = "Failed to update: ${e.message}"
                    }
                }
            } catch (e: Exception) {
                val isConnectivityError = e is ConnectException || e is UnknownHostException || e is SocketTimeoutException || e is HttpException
                if (isConnectivityError) {
                    setApiReachable(false)
                    _error.value = "API Unreachable."
                } else {
                    setApiReachable(true)
                    _error.value = "Operation failed: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun toggleControlMode() {
        val currentStationId = _stationId.value ?: return
        if (!_isApiReachable.value) {
            _error.value = "API unreachable."
            return
        }

        val currentMode = ControlMode.entries.find { it.name == uiState.value.controlMode } ?: ControlMode.ERROR
        if (currentMode == ControlMode.ERROR || !uiState.value.canToggleMode) {
            _error.value = "Cannot toggle mode now."
            return
        }

        val newMode = if (currentMode == ControlMode.ASTRO) ControlMode.MANUAL else ControlMode.ASTRO
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val result = repository.updateControlMode(currentStationId, newMode)
                result.onSuccess { updatedStation ->
                    setApiReachable(true)
                    try {
                        notificationDao.insertLastKnownStates(listOf(updatedStation.toLastKnownStateEntity()))
                    } catch (dbException: Exception) {
                        Log.e("StationViewModel", "DB update failed", dbException)
                    }
                }.onFailure { e ->
                    val isConnectivityError = e is ConnectException || e is UnknownHostException || e is SocketTimeoutException || e is HttpException || e.cause is ConnectException
                    if (isConnectivityError) {
                        setApiReachable(false)
                        _error.value = "API Unreachable."
                    } else {
                        setApiReachable(true)
                        _error.value = "Failed to update: ${e.message}"
                    }
                }
            } catch (e: Exception) {
                val isConnectivityError = e is ConnectException || e is UnknownHostException || e is SocketTimeoutException || e is HttpException
                if (isConnectivityError) {
                    setApiReachable(false)
                    _error.value = "API Unreachable."
                } else {
                    setApiReachable(true)
                    _error.value = "Operation failed: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun mapToUiState(
        stationEntity: StationEntity?,
        isLoading: Boolean,
        errorMessage: String?,
        isApiReachable: Boolean
    ): StationDetailUiState {
        if (stationEntity == null) {
            val finalError = if (!isApiReachable) "API unreachable." else errorMessage ?: if (_stationId.value != null && !isLoading) "Station not found" else null
            return StationDetailUiState(
                isLoading = isLoading && isApiReachable,
                errorMessage = finalError,
                stationId = _stationId.value ?: ""
            )
        }

        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
        val formattedDate = try {
            dateFormat.format(Date(stationEntity.lastUpdated))
        } catch (e: Exception) { "Invalid date" }

        val isManualMode = stationEntity.controlMode == ControlMode.MANUAL
        val isNotInError = stationEntity.state != DeviceState.ERROR && stationEntity.controlMode != ControlMode.ERROR
        val enableControls = isApiReachable && !isLoading && isNotInError

        return StationDetailUiState(
            isLoading = isLoading && isApiReachable,
            errorMessage = errorMessage,
            stationId = stationEntity.id,
            stationName = stationEntity.name,
            status = stationEntity.state.name,
            controlMode = stationEntity.controlMode.name,
            lastUpdated = formattedDate,
            latitude = stationEntity.location.latitude.toString(),
            longitude = stationEntity.location.longitude.toString(),
            canToggleState = enableControls && isManualMode,
            canToggleMode = enableControls
        )
    }
}