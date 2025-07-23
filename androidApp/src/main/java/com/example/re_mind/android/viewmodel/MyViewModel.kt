package com.example.re_mind.android.viewmodel

import ReminderEntity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.re_mind.android.db.GeofenceEntity
import com.example.re_mind.android.db.ReminderDao
import com.example.re_mind.android.geofence.GeofenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyViewModel: ViewModel() {
        private val _reminderList = MutableStateFlow(mutableListOf(ReminderEntity(0, "some_id", "title", 0L)))

    val reminderList: StateFlow<MutableList<ReminderEntity>> = _reminderList.asStateFlow()

    fun updateReminder(newReminder: ReminderEntity) {
        _reminderList.value = _reminderList.value.map { if (it.id == newReminder.id) newReminder else it } as MutableList

    }

    suspend fun addReminder( reminderDao: ReminderDao, newReminder: ReminderEntity) {
        viewModelScope.launch {
            reminderDao.insert(newReminder)
            _reminderList.value.add(newReminder)
        }
    }

    fun getReminderById(id: Long): ReminderEntity {
        return _reminderList.value.find { it.id == id } ?: ReminderEntity(0, "some_id", "title")
    }

    fun saveAndRegisterGeofence(entity: GeofenceEntity, geofenceManager: GeofenceManager) {
        viewModelScope.launch {
            geofenceManager.addGeofence(entity.id, entity.longitude, entity.latitude)
           geofenceManager.registerGeofence()
        }
    }
}