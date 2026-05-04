package com.example.myapplication.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "search_prefs")

class SearchPrefsRepository(private val context: Context) {
    private val RECENT_SEARCHES_KEY = stringPreferencesKey("recent_searches")

    val recentSearchesFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val searches = prefs[RECENT_SEARCHES_KEY] ?: ""
        if (searches.isEmpty()) emptyList() else searches.split("|")
    }

    suspend fun saveSearch(query: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[RECENT_SEARCHES_KEY] ?: ""
            val list = if (current.isEmpty()) mutableListOf() else current.split("|").toMutableList()
            list.remove(query)
            list.add(0, query)
            prefs[RECENT_SEARCHES_KEY] = list.take(5).joinToString("|")
        }
    }
}
