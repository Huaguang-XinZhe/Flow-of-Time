package com.huaguang.flowoftime.data

import android.content.SharedPreferences
import com.google.gson.Gson
import com.huaguang.flowoftime.EventType
import java.time.Duration

class SPHelper(private val sharedPreferences: SharedPreferences) {

    private val gson = Gson()

    fun saveState(
        isOneDayButtonClicked: Boolean,
        isInputShow: Boolean,
        buttonText: String,
        scrollIndex: Int,
        isTracking: Boolean,
        remainingDuration: Duration?,
        currentEvent: Event?,
        incompleteMainEvent: Event?,
        subButtonClickCount: Int,
        eventType: EventType,
        isLastStopFromSub: Boolean
    ) {
        val editor = sharedPreferences.edit()

        editor.putBoolean("IS_ONE_DAY_BUTTON_CLICKED", isOneDayButtonClicked)
        editor.putBoolean("is_input_show", isInputShow)
        editor.putString("button_text", buttonText)
        editor.putInt("scroll_index", scrollIndex)
        editor.putBoolean("isTracking", isTracking)
        editor.putInt("subButtonClickCount", subButtonClickCount)
        editor.putBoolean("isLastStopFromSub", isLastStopFromSub)

        if (remainingDuration != null) {
            val durationMillis = remainingDuration.toMillis()
            editor.putLong("remaining_duration", durationMillis)
        }

        if (currentEvent != null) {
            val eventJson = gson.toJson(currentEvent)
            editor.putString("currentEvent", eventJson)
        }

        if (incompleteMainEvent != null) {
            val eventJson = gson.toJson(incompleteMainEvent)
            editor.putString("incompleteMainEvent", eventJson)
        }

        val isSubEventType = eventType == EventType.SUB
        editor.putBoolean("is_sub_event_type", isSubEventType)

        editor.apply()
    }

    // 其他的 get 方法...

    fun getIsLastStopFromSub(): Boolean {
        return sharedPreferences.getBoolean("isLastStopFromSub", false)
    }
    fun getIsSubEventType(): Boolean {
        return sharedPreferences.getBoolean("is_sub_event_type", false)
    }

    fun getIncompleteMainEvent(): Event? {
        val eventJson = sharedPreferences.getString("incompleteMainEvent", null)
            ?: return null
        return gson.fromJson(eventJson, Event::class.java)
    }

    fun getCurrentEvent(): Event? {
        val eventJson = sharedPreferences.getString("currentEvent", null)
            ?: return null
        return gson.fromJson(eventJson, Event::class.java)
    }

    fun getIsOneDayButtonClicked(): Boolean {
        return sharedPreferences.getBoolean("IS_ONE_DAY_BUTTON_CLICKED", false)
    }

    fun getIsTracking(): Boolean {
        return sharedPreferences.getBoolean("isTracking", false)
    }

    fun getIsInputShow(): Boolean {
        return sharedPreferences.getBoolean("is_input_show", false)
    }

    fun getButtonText(): String {
        return sharedPreferences.getString("button_text", "开始") ?: "开始"
    }

    fun getSubButtonClickCount(): Int {
        return sharedPreferences.getInt("subButtonClickCount", 0)
    }

    fun getScrollIndex(): Int {
        return sharedPreferences.getInt("scroll_index", -1)
    }

    fun getRemainingDuration(): Duration? {
        val durationMillis = sharedPreferences.getLong("remaining_duration", -1L)
        return if (durationMillis != -1L) {
            Duration.ofMillis(durationMillis)
        } else null
    }

}

