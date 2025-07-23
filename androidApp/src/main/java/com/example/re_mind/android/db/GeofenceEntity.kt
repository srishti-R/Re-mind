package com.example.re_mind.android.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.location.Geofence

@Entity(tableName = "geofences")
data class GeofenceEntity(
    @PrimaryKey val id: String, // Unique ID for the geofence (can be the request ID)
    val latitude: Double,
    val longitude: Double,
    val radius: Float = 100f,
    val address: String? = null, // Optional address
    val transitionType: Int = Geofence.GEOFENCE_TRANSITION_ENTER, // e.g., Geofence.GEOFENCE_TRANSITION_ENTER
    val expirationTimeMillis: Long? = null,
    val registeredTimeMillis: Long = System.currentTimeMillis()
)