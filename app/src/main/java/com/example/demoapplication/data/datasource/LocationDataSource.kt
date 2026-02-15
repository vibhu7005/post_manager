package com.example.demoapplication.data.datasource

import com.example.demoapplication.data.model.LocationData
import com.example.demoapplication.data.model.LocationUpdate
import com.example.demoapplication.utils.Logger
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit
class LocationDataSource {
    
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MINUTES) // No timeout for SSE
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    companion object {
        private const val TAG = "LocationDataSource"
        private const val SSE_ENDPOINT = "https://your-server.com/api/location-stream"
    }
    
    fun subscribeToLocationUpdates(): Flow<Result<LocationUpdate>> = callbackFlow {
        Logger.d(TAG, "Starting SSE connection to location updates")
        
        val request = Request.Builder()
            .url(SSE_ENDPOINT)
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .build()
        
        val eventSourceListener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                Logger.i(TAG, "SSE connection opened successfully")
                trySend(Result.success(createConnectionUpdate("connected")))
            }
            
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                Logger.d(TAG, "Received SSE event: type=$type, data=$data")
                
                try {
                    when (type) {
                        "location_update", null -> {
                            val locationUpdate = gson.fromJson(data, LocationUpdate::class.java)
                            trySend(Result.success(locationUpdate))
                        }
                        "heartbeat" -> {
                            Logger.v(TAG, "Heartbeat received")
                        }
                        "error" -> {
                            Logger.w(TAG, "Server sent error: $data")
                            trySend(Result.failure(Exception("Server error: $data")))
                        }
                        else -> {
                            Logger.w(TAG, "Unknown event type: $type")
                        }
                    }
                } catch (e: Exception) {
                    Logger.e(TAG, "Error parsing SSE data: $data", e)
                    trySend(Result.failure(e))
                }
            }
            
            override fun onClosed(eventSource: EventSource) {
                Logger.i(TAG, "SSE connection closed")
                trySend(Result.failure(Exception("Connection closed")))
            }
            
            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: Response?
            ) {
                val errorMsg = "SSE connection failed: ${t?.message} (Response: ${response?.code})"
                Logger.e(TAG, errorMsg, t)
                trySend(Result.failure(t ?: Exception(errorMsg)))
            }
        }
        
        val eventSource = EventSources.createFactory(client)
            .newEventSource(request, eventSourceListener)
        
        awaitClose {
            Logger.d(TAG, "Closing SSE connection")
            eventSource.cancel()
        }
    }
    
    private fun createConnectionUpdate(status: String): LocationUpdate {
        return LocationUpdate(
            userId = "system",
            location = LocationData(
                latitude = 0.0,
                longitude = 0.0,
                timestamp = System.currentTimeMillis()
            ),
            eventType = "connection_$status"
        )
    }
}