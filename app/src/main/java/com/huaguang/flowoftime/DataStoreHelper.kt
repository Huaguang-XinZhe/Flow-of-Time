package com.huaguang.flowoftime

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.huaguang.flowoftime.utils.LocalDateTimeSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "event_datastore")

class DataStoreHelper @Inject constructor(
    private val context: Context,
) {

    private val startCursor = stringPreferencesKey("start_cursor")
    private val subEventCount = intPreferencesKey("sub_event_count")

    val startCursorFlow: Flow<LocalDateTime?> = context.dataStore.data
        .map { preferences ->
            val startCursorJson = preferences[startCursor]

            if (startCursorJson.isNullOrEmpty()) null else {
                Json.decodeFromString(startCursorJson)
            }
        }

    val subEventCountFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[subEventCount] ?: 0
        }


    suspend fun saveStartCursor(value: LocalDateTime?) {
        context.dataStore.edit { preferences ->
            val startCursorJson = if (value != null) {
                Json.encodeToString(LocalDateTimeSerializer, value)
            } else ""

            preferences[startCursor] = startCursorJson
        }
    }

    suspend fun saveSubEventCount(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[subEventCount] = value
        }
    }

}
