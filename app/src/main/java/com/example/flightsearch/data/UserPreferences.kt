package com.example.flightsearch.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val SEARCH_QUERY = stringPreferencesKey("search_query")
    }

    private val dataStore = context.dataStore

    val searchQueryFlow: Flow<String> = dataStore.data
        .map { prefs -> prefs[SEARCH_QUERY] ?: "" }

    suspend fun saveSearchQuery(query: String) {
        dataStore.edit { prefs ->
            prefs[SEARCH_QUERY] = query
        }
    }
}
