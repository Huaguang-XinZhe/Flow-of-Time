package com.huaguang.flowoftime.data

import android.content.SharedPreferences
import com.huaguang.flowoftime.EventStatus
import com.huaguang.flowoftime.data.models.Event
import kotlinx.serialization.json.Json
import java.time.Duration

class SPHelper(private val sharedPreferences: SharedPreferences) {

    fun saveState(
        isOneDayButtonClicked: Boolean,
        isInputShow: Boolean,
        buttonText: String,
        subButtonText: String,
        scrollIndex: Int,
        coreDuration: Duration,
        currentEvent: Event?,
        eventStatus: EventStatus,
        isLastStopFromSub: Boolean,
    ) {
        sharedPreferences.edit().apply {
            putBoolean("IS_ONE_DAY_BUTTON_CLICKED", isOneDayButtonClicked)
            putBoolean("is_input_show", isInputShow)
            putString("button_text", buttonText)
            putString("sub_button_text", subButtonText)
            putInt("scroll_index", scrollIndex)
            putBoolean("isLastStopFromSub", isLastStopFromSub)
            putLong("core_duration", coreDuration.toMillis())
            putInt("event_status_value", eventStatus.value)

            if (currentEvent != null) {
                val eventJson = Json.encodeToString(Event.serializer(), currentEvent)
                putString("currentEvent", eventJson)
            }

            apply()
        }
    }

    fun getAllData(): SPData {
        val isOneDayButtonClicked = getIsOneDayButtonClicked()
        val isInputShow = getIsInputShow()
        val buttonText = getButtonText()
        val subButtonText = getSubButtonText()
        val coreDuration = getCoreDuration()

        val eventStatus = getEventStatus()
        val isTracking = eventStatus.value != 0
        val currentEvent = if (isTracking) getCurrentEvent() else null

        val scrollIndex = getScrollIndex()
        val isLastStopFromSub = getIsLastStopFromSub()

        // 将获取的所有数据封装在 SharedPreferencesData 类的实例中
        return SPData(
            isOneDayButtonClicked,
            isInputShow,
            buttonText,
            subButtonText,
            coreDuration,
            eventStatus,
            currentEvent,
            scrollIndex,
            isLastStopFromSub,
        )
    }


    private fun getIsLastStopFromSub(): Boolean {
        return sharedPreferences.getBoolean("isLastStopFromSub", false)
    }
    private fun getEventStatus(): EventStatus {
        val eventStatusValue = sharedPreferences.getInt("event_status_value", 0)
        return EventStatus.fromInt(eventStatusValue)
    }

    private fun getCurrentEvent(): Event? {
        val eventJson = sharedPreferences.getString("currentEvent", null)
            ?: return null
        return Json.decodeFromString<Event>(eventJson)
    }

    private fun getIsOneDayButtonClicked(): Boolean {
        return sharedPreferences.getBoolean("IS_ONE_DAY_BUTTON_CLICKED", false)
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

    private fun getScrollIndex(): Int {
        return sharedPreferences.getInt("scroll_index", -1)
    }

    private fun getCoreDuration(): Duration {
        val durationMillis = sharedPreferences.getLong("core_duration", -1L)
        return if (durationMillis != -1L) {
            Duration.ofMillis(durationMillis)
        } else Duration.ZERO
    }

}

