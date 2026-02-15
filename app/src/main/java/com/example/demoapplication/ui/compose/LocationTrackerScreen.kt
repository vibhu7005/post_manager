package com.example.demoapplication.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.demoapplication.data.model.ConnectionStatus
import com.example.demoapplication.data.model.ErrorType
import com.example.demoapplication.data.model.LocationData
import com.example.demoapplication.data.model.LocationUiState
import com.example.demoapplication.ui.viewmodel.LocationViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LocationTrackerScreen(
    viewModel: LocationViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val lastLocationData by viewModel.lastLocationData.collectAsStateWithLifecycle()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with connection status
        LocationHeader(
            connectionStatus = connectionStatus,
            onStartTracking = { viewModel.startLocationTracking() },
            onStopTracking = { viewModel.stopLocationTracking() }
        )
        
        // Connection status indicator
        ConnectionStatusCard(connectionStatus = connectionStatus)
        
        // Main content based on UI state
        when (uiState) {
            is LocationUiState.Idle -> {
                IdleContent()
            }
            is LocationUiState.Loading -> {
                LoadingContent()
            }
            is LocationUiState.Connecting -> {
                ConnectingContent()
            }
            is LocationUiState.Connected -> {
                ConnectedContent((uiState as LocationUiState.Connected).isReceivingUpdates)
            }
            is LocationUiState.LocationReceived -> {
                LocationContent(
                    location = (uiState as LocationUiState.LocationReceived).location,
                    connectionStatus = (uiState as LocationUiState.LocationReceived).connectionStatus
                )
            }
            is LocationUiState.Error -> {
                ErrorContent(
                    error = uiState as LocationUiState.Error,
                    onRetry = { viewModel.retryConnection() }
                )
            }
            is LocationUiState.Disconnected -> {
                DisconnectedContent(
                    state = uiState as LocationUiState.Disconnected,
                    onRetry = { viewModel.retryConnection() }
                )
            }
        }
        
        // Last known location
        lastLocationData?.let { location ->
            LastLocationCard(location = location)
        }
    }
}

@Composable
private fun LocationHeader(
    connectionStatus: ConnectionStatus,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Location Tracker",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        when (connectionStatus) {
            ConnectionStatus.DISCONNECTED -> {
                Button(
                    onClick = onStartTracking,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Start",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start")
                }
            }
            else -> {
                Button(
                    onClick = onStopTracking,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Stop")
                }
            }
        }
    }
}

@Composable
private fun ConnectionStatusCard(
    connectionStatus: ConnectionStatus,
    modifier: Modifier = Modifier
) {
    val (statusText, statusColor) = when (connectionStatus) {
        ConnectionStatus.DISCONNECTED -> "Disconnected" to Color.Gray
        ConnectionStatus.CONNECTING -> "Connecting..." to Color(0xFFFF9800)
        ConnectionStatus.CONNECTED -> "Connected" to Color.Green
        ConnectionStatus.RECONNECTING -> "Reconnecting..." to Color(0xFFFF9800)
        ConnectionStatus.ERROR -> "Connection Error" to Color.Red
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = statusColor,
                        shape = RoundedCornerShape(6.dp)
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = statusText,
                fontWeight = FontWeight.Medium,
                color = statusColor
            )
        }
    }
}

@Composable
private fun IdleContent() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Ready to Track Location",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Tap 'Start' to begin receiving location updates",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Initializing location services...")
        }
    }
}

@Composable
private fun ConnectingContent() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Connecting to location service...")
        }
    }
}

@Composable
private fun ConnectedContent(isReceivingUpdates: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Connected",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isReceivingUpdates) "Receiving Updates" else "Connected",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun LocationContent(
    location: LocationData,
    connectionStatus: ConnectionStatus
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Current Location",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LocationInfoRow("Latitude", String.format("%.6f", location.latitude))
            LocationInfoRow("Longitude", String.format("%.6f", location.longitude))
            location.accuracy?.let {
                LocationInfoRow("Accuracy", "${String.format("%.1f", it)}m")
            }
            location.speed?.let {
                LocationInfoRow("Speed", "${String.format("%.1f", it * 3.6f)} km/h")
            }
            LocationInfoRow(
                "Updated",
                SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(location.timestamp))
            )
        }
    }
}

@Composable
private fun LocationInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ErrorContent(
    error: LocationUiState.Error,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Connection Error",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error.message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            if (error.canRetry) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun DisconnectedContent(
    state: LocationUiState.Disconnected,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.willRetry) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Reconnecting...",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Attempt ${state.retryAttempt}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Disconnected",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Disconnected",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = state.reason,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRetry) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun LastLocationCard(
    location: LocationData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Last Known Location",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault()).format(Date(location.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}