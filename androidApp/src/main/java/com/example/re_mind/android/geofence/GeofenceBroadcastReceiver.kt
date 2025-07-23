package com.example.re_mind.android.geofence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent



class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "CUSTOM_INTENT_GEOFENCE") {
            GeofencingEvent.fromIntent(intent)?.let { geofencingEvent ->

                if (geofencingEvent.hasError()) {
                    Log.e("GeofenceReceiver", "Error: ${geofencingEvent.errorCode}")
                    return
                }

                val transition = geofencingEvent.geofenceTransition
                val triggeredGeofences = geofencingEvent.triggeringGeofences

                if (transition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                    transition == Geofence.GEOFENCE_TRANSITION_EXIT) {

                    val transitionType = when (transition) {
                        Geofence.GEOFENCE_TRANSITION_ENTER -> "Entered"
                        Geofence.GEOFENCE_TRANSITION_EXIT -> "Exited"
                        else -> "Unknown"
                    }

                    val notificationText = "$transitionType geofence: ${triggeredGeofences?.firstOrNull()?.requestId}"
                    showNotification(context, notificationText)
                }
            }
        }
    }

    private fun showNotification(context: Context, message: String) {
        val channelId = "geofence_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Geofence Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle("Geofence Triggered")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
