package com.example.demoapplication.data.repository

import com.example.demoapplication.data.datasource.LocationDataSource
import com.example.demoapplication.data.model.LocationUpdate
import com.example.demoapplication.utils.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import kotlin.math.min
import kotlin.math.pow

class LocationRepositoryImpl(
    private val locationDataSource: LocationDataSource
) : LocationRepository {
    
    companion object {
        private const val TAG = "LocationRepository"
        private const val MAX_RETRY_ATTEMPTS = 5
        private const val INITIAL_RETRY_DELAY_MS = 1000L
        private const val MAX_RETRY_DELAY_MS = 30000L
        private const val BACKOFF_MULTIPLIER = 2.0
    }
    
    override fun getLocationUpdates(): Flow<Result<LocationUpdate>> = flow {
        Logger.performanceLog(TAG, "location_stream_setup") {
            locationDataSource.subscribeToLocationUpdates()
                .retryWhen { cause, attempt ->
                    val shouldRetry = attempt < MAX_RETRY_ATTEMPTS
                    
                    if (shouldRetry) {
                        val delayMs = calculateBackoffDelay(attempt)
                        Logger.w(
                            TAG,
                            "Connection failed (attempt ${attempt + 1}/$MAX_RETRY_ATTEMPTS), " +
                                    "retrying in ${delayMs}ms",
                            cause
                        )
                        
                        emit(Result.failure(Exception(
                            "Connection failed, retrying in ${delayMs}ms (attempt ${attempt + 1}/$MAX_RETRY_ATTEMPTS)"
                        )))
                        
                        delay(delayMs)
                        true
                    } else {
                        Logger.e(TAG, "Max retry attempts reached, giving up", cause)
                        emit(Result.failure(Exception(
                            "Failed to establish connection after $MAX_RETRY_ATTEMPTS attempts: ${cause.message}"
                        )))
                        false
                    }
                }
                .catch { exception ->
                    Logger.e(TAG, "Unrecoverable error in location stream", exception)
                    emit(Result.failure(exception))
                }
                .collect { result ->
                    emit(result)
                }
        }
    }
    
    override suspend fun startLocationTracking() {
        Logger.i(TAG, "Starting location tracking")
    }
    
    override suspend fun stopLocationTracking() {
        Logger.i(TAG, "Stopping location tracking")
    }
    
    private fun calculateBackoffDelay(attempt: Long): Long {
        val exponentialDelay = (INITIAL_RETRY_DELAY_MS * 
            BACKOFF_MULTIPLIER.pow(attempt.toDouble())).toLong()
        
        val jitteredDelay = exponentialDelay + (Math.random() * 1000).toLong()
        
        return min(jitteredDelay, MAX_RETRY_DELAY_MS)
    }
}