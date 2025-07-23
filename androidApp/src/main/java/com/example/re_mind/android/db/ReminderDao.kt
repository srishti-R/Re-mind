package com.example.re_mind.android.db

import ReminderEntity
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity): Long // Returns the new rowId

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Delete
    suspend fun delete(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE geofenceId = :geofenceId ORDER BY creationTimeMillis DESC")
    fun getRemindersForGeofence(geofenceId: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 AND isTriggered = 0 ORDER BY creationTimeMillis DESC")
    fun getAllActiveReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders ORDER BY creationTimeMillis DESC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("DELETE FROM reminders")
    suspend fun clearAll()
}