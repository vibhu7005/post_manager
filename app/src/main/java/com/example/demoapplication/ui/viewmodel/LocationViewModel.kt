package com.example.demoapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapplication.data.model.ConnectionStatus
import com.example.demoapplication.data.model.ErrorType
import com.example.demoapplication.data.model.LocationData
import com.example.demoapplication.data.model.LocationUiState
import com.example.demoapplication.data.model.LocationUpdate
import com.example.demoapplication.data.repository.LocationRepository
import com.example.demoapplication.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
class LocationViewModel(
    private val locationRepository: LocationRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "LocationViewModel"
        private const val STATE_TIMEOUT_MS = 5000L
    }
    
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus
    
    private val _lastLocationData = MutableStateFlow<LocationData?>(null)
    val lastLocationData: StateFlow<LocationData?> = _lastLocationData
    
    private val _retryAttempt = MutableStateFlow(0)
    
    val uiState: StateFlow<LocationUiState> = locationRepository.getLocationUpdates()
        .map { result ->
            result.fold(
                onSuccess = { locationUpdate ->
                    handleSuccessfulUpdate(locationUpdate)
                },
                onFailure = { error ->
                    handleError(error)
                }
            )
        }
        .onStart {
            Logger.d(TAG, "Starting location updates stream")
            emit(LocationUiState.Loading)
        }
        .catch { error ->
            Logger.e(TAG, "Error in location updates stream", error)
            emit(LocationUiState.Error(
                message = error.message ?: "Unknown error occurred",
                errorType = ErrorType.UNKNOWN_ERROR
            ))
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS),
            initialValue = LocationUiState.Idle
        )
    
    fun startLocationTracking() {
        Logger.d(TAG, "Starting location tracking")
        viewModelScope.launch {
            try {
                _connectionStatus.value = ConnectionStatus.CONNECTING
                locationRepository.startLocationTracking()
                Logger.i(TAG, "Location tracking started successfully")
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to start location tracking", e)
                _connectionStatus.value = ConnectionStatus.ERROR
            }
        }
    }
    
    fun stopLocationTracking() {
        Logger.d(TAG, "Stopping location tracking")
        viewModelScope.launch {
            try {
                locationRepository.stopLocationTracking()
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
                _lastLocationData.value = null
                _retryAttempt.value = 0
                Logger.i(TAG, "Location tracking stopped successfully")
            } catch (e: Exception) {
                Logger.e(TAG, "Error while stopping location tracking", e)
            }
        }
    }
    
    fun retryConnection() {
        Logger.d(TAG, "Retrying connection")
        _retryAttempt.value = _retryAttempt.value + 1
        startLocationTracking()
    }
    
    private fun handleSuccessfulUpdate(locationUpdate: LocationUpdate): LocationUiState {
        return when (locationUpdate.eventType) {
            "connection_connected" -> {
                _connectionStatus.value = ConnectionStatus.CONNECTED
                _retryAttempt.value = 0
                LocationUiState.Connected(isReceivingUpdates = false)
            }
            "location_update" -> {
                _connectionStatus.value = ConnectionStatus.CONNECTED
                _lastLocationData.value = locationUpdate.location
                _retryAttempt.value = 0
                Logger.v(TAG, "Received location update: ${locationUpdate.location}")
                LocationUiState.LocationReceived(
                    location = locationUpdate.location,
                    connectionStatus = ConnectionStatus.CONNECTED
                )
            }
            else -> {
                Logger.w(TAG, "Unknown event type: ${locationUpdate.eventType}")
                LocationUiState.Connected(isReceivingUpdates = true)
            }
        }
    }
    
    private fun handleError(error: Throwable): LocationUiState {
        val currentRetryAttempt = _retryAttempt.value
        
        return when {
            error.message?.contains("retrying") == true -> {
                _connectionStatus.value = ConnectionStatus.RECONNECTING
                LocationUiState.Disconnected(
                    reason = error.message ?: "Connection lost",
                    willRetry = true,
                    retryAttempt = currentRetryAttempt
                )
            }
            error.message?.contains("Max retry attempts") == true -> {
                _connectionStatus.value = ConnectionStatus.ERROR
                LocationUiState.Error(
                    message = "Unable to connect to location service",
                    errorType = ErrorType.SSE_CONNECTION_FAILED,
                    canRetry = true
                )
            }
            else -> {
                _connectionStatus.value = ConnectionStatus.ERROR
                LocationUiState.Error(
                    message = error.message ?: "Connection error",
                    errorType = determineErrorType(error),
                    canRetry = true
                )
            }
        }
    }
    
    private fun determineErrorType(error: Throwable): ErrorType {
        return when {
            error.message?.contains("network", ignoreCase = true) == true -> ErrorType.NETWORK_ERROR
            error.message?.contains("permission", ignoreCase = true) == true -> ErrorType.PERMISSION_DENIED
            error.message?.contains("location", ignoreCase = true) == true -> ErrorType.LOCATION_DISABLED
            error.message?.contains("connection", ignoreCase = true) == true -> ErrorType.SSE_CONNECTION_FAILED
            else -> ErrorType.UNKNOWN_ERROR
        }
    }
    
    override fun onCleared() {
        Logger.d(TAG, "ViewModel cleared, stopping location tracking")
        stopLocationTracking()
        super.onCleared()
    }
}