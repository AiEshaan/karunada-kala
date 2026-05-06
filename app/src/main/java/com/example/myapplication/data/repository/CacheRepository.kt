package com.example.myapplication.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "cultural_cache")

class CacheRepository(private val context: Context) {
    private val gson = Gson()

    suspend fun <T> saveCollection(key: String, data: List<T>) {
        val json = gson.toJson(data)
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = json
        }
    }

    suspend fun <T> getCollection(key: String, typeToken: TypeToken<List<T>>): List<T> {
        val preferences = context.dataStore.data.first()
        val json = preferences[stringPreferencesKey(key)] ?: return emptyList()
        return try {
            gson.fromJson(json, typeToken.type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun <T> observeCollection(key: String, typeToken: TypeToken<List<T>>): Flow<List<T>> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[stringPreferencesKey(key)] ?: return@map emptyList<T>()
            try {
                gson.fromJson<List<T>>(json, typeToken.type)
            } catch (e: Exception) {
                emptyList<T>()
            }
        }
    }
}
