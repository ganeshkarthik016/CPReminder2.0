package com.example.cpreminder20

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class PreferenceManager(private val context: Context) {

    companion object {
        val KEY_HANDLE = stringPreferencesKey("cf_handle")
        val KEY_CONTEST_ALARM = booleanPreferencesKey("contest_alarm_enabled") // Existing
        val KEY_DAILY_CHECK = booleanPreferencesKey("daily_check_enabled")     // NEW
    }

    // Save/Get Codeforces Handle
    suspend fun saveHandle(handle: String) {
        context.dataStore.edit { it[KEY_HANDLE] = handle }
    }
    val getHandle: Flow<String?> = context.dataStore.data.map { it[KEY_HANDLE] }

    // Save/Get Contest Alarm Switch
    suspend fun setContestAlarm(enabled: Boolean) {
        context.dataStore.edit { it[KEY_CONTEST_ALARM] = enabled }
    }
    val isContestAlarmOn: Flow<Boolean> = context.dataStore.data.map { it[KEY_CONTEST_ALARM] ?: true }

    // Save/Get Daily 10:30 PM Check Switch
    suspend fun setDailyCheck(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DAILY_CHECK] = enabled }
    }
    val isDailyCheckOn: Flow<Boolean> = context.dataStore.data.map { it[KEY_DAILY_CHECK] ?: true }
}