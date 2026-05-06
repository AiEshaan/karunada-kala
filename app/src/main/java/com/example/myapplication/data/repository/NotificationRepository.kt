package com.example.myapplication.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.data.model.AppNotification
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.notificationDataStore by preferencesDataStore(name = "notifications")

class NotificationRepository(private val context: Context) {
    private val gson = Gson()
    private val key = stringPreferencesKey("notification_list")

    val notifications: Flow<List<AppNotification>> = context.notificationDataStore.data.map { preferences ->
        val json = preferences[key] ?: return@map emptyList<AppNotification>()
        try {
            val type = object : TypeToken<List<AppNotification>>() {}.type
            gson.fromJson<List<AppNotification>>(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addNotification(notification: AppNotification) {
        context.notificationDataStore.edit { preferences ->
            val currentJson = preferences[key]
            val currentList = if (currentJson != null) {
                val type = object : TypeToken<List<AppNotification>>() {}.type
                gson.fromJson<MutableList<AppNotification>>(currentJson, type)
            } else {
                mutableListOf()
            }
            currentList.add(0, notification)
            preferences[key] = gson.toJson(currentList)
        }
    }

    suspend fun markAsRead(notificationId: String) {
        context.notificationDataStore.edit { preferences ->
            val currentJson = preferences[key] ?: return@edit
            val type = object : TypeToken<List<AppNotification>>() {}.type
            val currentList = gson.fromJson<MutableList<AppNotification>>(currentJson, type)
            val index = currentList.indexOfFirst { it.id == notificationId }
            if (index != -1) {
                currentList[index] = currentList[index].copy(isRead = true)
                preferences[key] = gson.toJson(currentList)
            }
        }
    }
}
