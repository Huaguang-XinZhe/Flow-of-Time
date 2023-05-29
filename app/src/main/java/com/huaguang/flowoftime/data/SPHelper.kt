package com.huaguang.flowoftime.data

import android.content.SharedPreferences
import java.time.Duration

class SPHelper(private val sharedPreferences: SharedPreferences) {

    fun saveIsOneDayButtonClicked(value: Boolean) {
        sharedPreferences.edit().putBoolean("IS_ONE_DAY_BUTTON_CLICKED", value).apply()
    }

    fun getIsOneDayButtonClicked(): Boolean {
        return sharedPreferences.getBoolean("IS_ONE_DAY_BUTTON_CLICKED", false)
    }

    fun getIsTracking(): Boolean {
        return sharedPreferences.getBoolean("isTracking", false)
    }

    fun setIsTracking(isTracking: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean("isTracking", isTracking)
            apply()
        }
    }

    fun saveButtonText(text: String) {
        sharedPreferences.edit().putString("button_text", text).apply()
    }

    fun getButtonText(): String? {
        return sharedPreferences.getString("button_text", "开始")
    }

    fun getSubButtonText(): String? {
        return sharedPreferences.getString("sub_button_text", "插入")
    }

    fun saveSubButtonText(text: String) {
        sharedPreferences.edit().putString("sub_button_text", text).apply()
    }

    fun saveScrollIndex(index: Int) {
        sharedPreferences.edit().putInt("scroll_index", index).apply()
    }

    fun getScrollIndex(): Int {
        return sharedPreferences.getInt("scroll_index", -1)
    }

    fun saveRemainingDuration(duration: Duration) {
        val durationMillis = duration.toMillis()
        sharedPreferences.edit().putLong("remaining_duration", durationMillis).apply()
    }

    fun getRemainingDuration(): Duration? {
        val durationMillis = sharedPreferences.getLong("remaining_duration", -1L)
        return if (durationMillis != -1L) {
            Duration.ofMillis(durationMillis)
        } else null
    }

}
