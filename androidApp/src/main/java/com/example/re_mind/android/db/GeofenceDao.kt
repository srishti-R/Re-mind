package com.example.re_mind.android.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GeofenceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(geofenceLocation: GeofenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(geofenceLocations: List<GeofenceEntity>)

    @Update
    suspend fun update(geofenceLocation: GeofenceEntity)

    @Delete
    suspend fun delete(geofenceLocation: GeofenceEntity)

    @Query("DELETE FROM geofences WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM geofences WHERE id = :id")
    suspend fun getGeofenceById(id: String): GeofenceEntity?

    @Query("SELECT * FROM geofences ORDER BY registeredTimeMillis DESC")
    fun getAllGeofences(): Flow<List<GeofenceEntity>> // Observe changes with Flow

    @Query("DELETE FROM geofences")
    suspend fun clearAll()
}