package com.example.re_mind.android.geofence

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.re_mind.android.db.AppDatabase
import com.example.re_mind.android.db.GeofenceEntity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await



class GeofenceManager(context: Context) {
    private val TAG = "GeofenceManager"
    private val client = LocationServices.getGeofencingClient(context)
    val geofenceList = mutableMapOf<String, Geofence>()
    private val CUSTOM_REQUEST_CODE_GEOFENCE = 1234
    private val CUSTOM_INTENT_GEOFENCE = "CUSTOM_INTENT_GEOFENCE"
    private val geofenceDao = AppDatabase.getInstance(context).geofenceDao()



    private val geofencingPendingIntent by lazy {
        PendingIntent.getBroadcast(
            context,
            CUSTOM_REQUEST_CODE_GEOFENCE,
            Intent(CUSTOM_INTENT_GEOFENCE),
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                PendingIntent.FLAG_CANCEL_CURRENT
            } else {
                PendingIntent.FLAG_MUTABLE
            }
        )
    }

    fun addGeofence(
        key: String,
        longitude: Double,
        latitude: Double,
        radiusInMeters: Float = 100.0f,
        expirationTimeInMillis: Long = 30 * 60 * 1000,
    ) {
        geofenceList[key] = createGeofence(key, longitude, latitude, radiusInMeters, expirationTimeInMillis)
    }

    fun removeGeofence(key: String) {
        geofenceList.remove(key)
    }

    @SuppressLint("MissingPermission")
     fun registerGeofence() {
        client.addGeofences(createGeofencingRequest(), geofencingPendingIntent)
            .addOnSuccessListener {
                // Save to Room after successful add
                CoroutineScope(Dispatchers.IO).launch {
                    geofenceDao.insertAll(geofenceList.map { (key, geofence) ->
                        GeofenceEntity(
                            id = key,
                            latitude = geofence.latitude,
                            longitude = geofence.longitude)
                    })
                }
                Log.d(TAG, "registerGeofence: SUCCESS")
            }.addOnFailureListener { exception ->
                Log.d(TAG, "registerGeofence: Failure\n$exception")
            }
    }

    suspend fun deregisterGeofence() = kotlin.runCatching {
        client.removeGeofences(geofencingPendingIntent).await()
        geofenceList.clear()
    }

    private fun createGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GEOFENCE_TRANSITION_ENTER)
            addGeofences(geofenceList.values.toList())
        }.build()
    }

    private fun createGeofence(
        key: String,
        longitude: Double,
        latitude: Double,
        radiusInMeters: Float,
        expirationTimeInMillis: Long,
    ): Geofence {
        return Geofence.Builder()
            .setRequestId(key)
            .setCircularRegion(latitude, longitude, radiusInMeters)
            .setExpirationDuration(expirationTimeInMillis)
            .setTransitionTypes(GEOFENCE_TRANSITION_ENTER or GEOFENCE_TRANSITION_EXIT)
            .build()
    }



}