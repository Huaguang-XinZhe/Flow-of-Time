package com.huaguang.flowoftime.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Duration
import java.time.LocalDateTime

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "event_datastore")

class DataStoreHelper (
    private val context: Context,
    private val json: Json,
) {

    private val startCursor = stringPreferencesKey("start_cursor")
    private val subEventCount = intPreferencesKey("sub_event_count")
    private val deltaSum = longPreferencesKey("deltaSum")
//    private val isInputShowKey = booleanPreferencesKey("is_input_show")
    private val saveCoreDurationFlag = booleanPreferencesKey("save_core_duration_flag")


    val startCursorFlow: Flow<LocalDateTime?> = context.dataStore.data
        .map { preferences ->
            val startCursorJson = preferences[startCursor]

            if (startCursorJson.isNullOrEmpty()) null else {
                json.decodeFromString(startCursorJson)
            }
        }

    val subEventCountFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[subEventCount] ?: 0
        }

    val deltaSumFlow: Flow<Duration> = context.dataStore.data
        .map { preferences ->
            val deltaSumMillis = preferences[deltaSum] ?: 0L

            if (deltaSumMillis == 0L) Duration.ZERO else {
                Duration.ofMillis(deltaSumMillis)
            }
        }

//    val isInputShowFlow: Flow<Boolean> = context.dataStore.data
//        .map { preferences ->
//            preferences[isInputShowKey] ?: false
//        }

    val saveCoreDurationFlagFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[saveCoreDurationFlag] ?: false
        }




    suspend fun saveStartCursor(value: LocalDateTime?) {
        context.dataStore.edit { preferences ->
            val startCursorJson =
                if (value != null) {
                    json.encodeToString(value)
                } else ""

            preferences[startCursor] = startCursorJson
        }
    }

    suspend fun saveSubEventCount(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[subEventCount] = value
        }
    }

    /**
     * 本身这个 delta 是不可能为 null 的，但为了应对置空的场景，故特允为空
     */
    suspend fun saveDeltaSum(delta: Duration?) {
        context.dataStore.edit { preferences ->
            preferences[deltaSum] = if (delta != null) {
                val getDeltaSum = preferences[deltaSum] ?: 0L
                getDeltaSum + delta.toMillis()
            } else 0L
        }
    }

//    suspend fun saveIsInputShow(value: Boolean) {
//        context.dataStore.edit { preferences ->
//            preferences[isInputShowKey] = value
//        }
//    }

    suspend fun saveCoreDurationFlag(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[saveCoreDurationFlag] = value
        }
    }




}
