package com.example.demoapplication.data.repository

import com.example.demoapplication.data.model.LocationUpdate
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getLocationUpdates(): Flow<Result<LocationUpdate>>
    suspend fun startLocationTracking()
    suspend fun stopLocationTracking()
}