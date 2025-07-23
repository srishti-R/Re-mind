package com.example.re_mind.android.db

import ReminderEntity
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [GeofenceEntity::class, ReminderEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun geofenceDao(): GeofenceDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build().also { instance = it }
            }
        }
    }
}