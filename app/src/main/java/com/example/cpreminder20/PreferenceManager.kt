package com.example.cpreminder20

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// This creates the DataStore. It MUST be outside the class.
val Context.dataStore by preferencesDataStore(name = "user_prefs")

class PreferenceManager(private val context: Context) {

    // Keys
    private val HANDLE_KEY = stringPreferencesKey("cf_handle")
    private val ALARM_ENABLED_KEY = booleanPreferencesKey("alarm_enabled")

    // --- Handle Logic ---
    suspend fun saveHandle(handle: String) {
        context.dataStore.edit { prefs ->
            prefs[HANDLE_KEY] = handle
        }
    }

    val getHandle: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[HANDLE_KEY]
    }

    // --- Alarm Switch Logic ---
    suspend fun setAlarmsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ALARM_ENABLED_KEY] = enabled
        }
    }

    val isAlarmEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ALARM_ENABLED_KEY] ?: true // Default is TRUE (Alarms ON)
    }
}