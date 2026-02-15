package com.example.demoapplication.data.model

import com.google.gson.annotations.SerializedName

data class LocationData(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("accuracy")
    val accuracy: Float? = null,
    @SerializedName("speed")
    val speed: Float? = null,
    @SerializedName("bearing")
    val bearing: Float? = null
)

data class LocationUpdate(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("location")
    val location: LocationData,
    @SerializedName("eventType")
    val eventType: String = "location_update"
)