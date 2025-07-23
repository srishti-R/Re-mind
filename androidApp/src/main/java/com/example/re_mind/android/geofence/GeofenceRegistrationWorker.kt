package com.example.re_mind.android.geofence

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class GeofenceRegistrationWorker(val context: Context,
                                 workerParams: WorkerParameters
): CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val geofenceManager = GeofenceManager(context = context);
        geofenceManager.registerGeofence()
        return Result.success()
    }
}