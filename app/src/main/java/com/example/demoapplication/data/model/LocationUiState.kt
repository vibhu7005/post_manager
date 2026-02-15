package com.example.demoapplication.data.model

sealed class LocationUiState {
    object Idle : LocationUiState()
    object Loading : LocationUiState()
    object Connecting : LocationUiState()
    
    data class Connected(
        val isReceivingUpdates: Boolean = false
    ) : LocationUiState()
    
    data class LocationReceived(
        val location: LocationData,
        val connectionStatus: ConnectionStatus = ConnectionStatus.CONNECTED
    ) : LocationUiState()
    
    data class Error(
        val message: String,
        val errorType: ErrorType,
        val canRetry: Boolean = true
    ) : LocationUiState()
    
    data class Disconnected(
        val reason: String,
        val willRetry: Boolean = true,
        val retryAttempt: Int = 0
    ) : LocationUiState()
}

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    ERROR
}

enum class ErrorType {
    NETWORK_ERROR,
    PERMISSION_DENIED,
    LOCATION_DISABLED,
    SSE_CONNECTION_FAILED,
    UNKNOWN_ERROR
}