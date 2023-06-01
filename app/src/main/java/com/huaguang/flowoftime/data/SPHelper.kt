package com.huaguang.flowoftime.data

import android.content.SharedPreferences
import com.huaguang.flowoftime.EventType
import kotlinx.serialization.json.Json
import java.time.Duration

class SPHelper(private val sharedPreferences: SharedPreferences) {

    fun saveState(
        isOneDayButtonClicked: Boolean,
        isInputShow: Boolean,
        buttonText: String,
        subButtonText: String,
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
        editor.putString("sub_button_text", subButtonText)
        editor.putInt("scroll_index", scrollIndex)
        editor.putBoolean("isTracking", isTracking)
        editor.putInt("subButtonClickCount", subButtonClickCount)
        editor.putBoolean("isLastStopFromSub", isLastStopFromSub)

        if (remainingDuration != null) {
            val durationMillis = remainingDuration.toMillis()
            editor.putLong("remaining_duration", durationMillis)
        }

        if (currentEvent != null) {
            val eventJson = Json.encodeToString(Event.serializer(), currentEvent)
            editor.putString("currentEvent", eventJson)
        }

        if (incompleteMainEvent != null) {
            val eventJson = Json.encodeToString(Event.serializer(), incompleteMainEvent)
            editor.putString("incompleteMainEvent", eventJson)
        }

        val isSubEventType = eventType == EventType.SUB
        editor.putBoolean("is_sub_event_type", isSubEventType)

        editor.apply()
    }

    fun getAllData(): SPData {
        val isOneDayButtonClicked = getIsOneDayButtonClicked()
        val isInputShow = getIsInputShow()
        val buttonText = getButtonText()
        val subButtonText = getSubButtonText()
        val remainingDuration = getRemainingDuration()
        val isTracking = getIsTracking()
        val currentEvent = if (isTracking) getCurrentEvent() else null
        val incompleteMainEvent =  if (isTracking) getIncompleteMainEvent() else null
        val scrollIndex = getScrollIndex()
        val subButtonClickCount = getSubButtonClickCount()
        val isSubEventType = getIsSubEventType()
        val isLastStopFromSub = getIsLastStopFromSub()

        // 将获取的所有数据封装在 SharedPreferencesData 类的实例中
        return SPData(
            isOneDayButtonClicked,
            isInputShow,
            buttonText,
            subButtonText,
            remainingDuration,
            isTracking,
            currentEvent,
            incompleteMainEvent,
            scrollIndex,
            subButtonClickCount,
            isSubEventType,
            isLastStopFromSub
        )
    }


    fun getIsLastStopFromSub(): Boolean {
        return sharedPreferences.getBoolean("isLastStopFromSub", false)
    }
    private fun getIsSubEventType(): Boolean {
        return sharedPreferences.getBoolean("is_sub_event_type", false)
    }

    private fun getIncompleteMainEvent(): Event? {
        val eventJson = sharedPreferences.getString("incompleteMainEvent", null)
            ?: return null
        return Json.decodeFromString<Event>(eventJson)
    }

    private fun getCurrentEvent(): Event? {
        val eventJson = sharedPreferences.getString("currentEvent", null)
            ?: return null
        return Json.decodeFromString<Event>(eventJson)
    }

    private fun getIsOneDayButtonClicked(): Boolean {
        return sharedPreferences.getBoolean("IS_ONE_DAY_BUTTON_CLICKED", false)
    }

    private fun getIsTracking(): Boolean {
        return sharedPreferences.getBoolean("isTracking", false)
    }

    private fun getIsInputShow(): Boolean {
        return sharedPreferences.getBoolean("is_input_show", false)
    }

    private fun getButtonText(): String {
        return sharedPreferences.getString("button_text", "开始") ?: "开始"
    }

    private fun getSubButtonText(): String {
        return sharedPreferences.getString("sub_button_text", "插入") ?: "插入"
    }

    private fun getSubButtonClickCount(): Int {
        return sharedPreferences.getInt("subButtonClickCount", 0)
    }

    private fun getScrollIndex(): Int {
        return sharedPreferences.getInt("scroll_index", -1)
    }

    private fun getRemainingDuration(): Duration? {
        val durationMillis = sharedPreferences.getLong("remaining_duration", -1L)
        return if (durationMillis != -1L) {
            Duration.ofMillis(durationMillis)
        } else null
    }

}

