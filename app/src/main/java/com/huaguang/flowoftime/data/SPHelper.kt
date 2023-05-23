package com.huaguang.flowoftime.data

import android.content.SharedPreferences
import java.time.Duration

class SPHelper(private val sharedPreferences: SharedPreferences) {

    fun saveButtonText(text: String) {
        sharedPreferences.edit().putString("button_text", text).apply()
    }

    fun getButtonText(): String {
        return sharedPreferences.getString("button_text", "开始") ?: "开始"
    }

    fun saveScrollIndex(index: Int) {
        sharedPreferences.edit().putInt("scroll_index", index).apply()
    }

    fun getScrollIndex(): Int {
        return sharedPreferences.getInt("scroll_index", -1)
    }

    fun clearScrollIndex() {
        sharedPreferences.edit().remove("scroll_index").apply()
    }

    fun saveRemainingDuration(duration: Duration) {
        val durationMillis = duration.toMillis()
        sharedPreferences.edit().putLong("remaining_duration", durationMillis).apply()
    }

    fun getRemainingDuration(): Duration? {
        val durationMillis = sharedPreferences.getLong("remaining_duration", -1L)
        return if (durationMillis != -1L) {
            Duration.ofMillis(durationMillis)
        } else {
            null
        }
    }

}
