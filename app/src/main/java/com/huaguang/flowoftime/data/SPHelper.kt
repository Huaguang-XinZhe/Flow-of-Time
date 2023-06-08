package com.huaguang.flowoftime.data

import android.content.SharedPreferences
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.data.models.Event
import kotlinx.serialization.json.Json
import java.time.Duration
import java.time.LocalDateTime

class SPHelper(private val sharedPreferences: SharedPreferences) {

    fun saveState(
        isOneDayButtonClicked: Boolean,
        isInputShow: Boolean,
        buttonText: String,
        subButtonText: String,
        scrollIndex: Int,
        isTracking: Boolean,
        isCoreEventTracking: Boolean,
        coreDuration: Duration,
        startTimeTracking: LocalDateTime?,
        currentEvent: Event?,
        incompleteMainEvent: Event?,
        subButtonClickCount: Int,
        eventType: EventType,
        isLastStopFromSub: Boolean,
        isCoreDurationReset: Boolean
    ) {
        sharedPreferences.edit().apply {
            putBoolean("IS_ONE_DAY_BUTTON_CLICKED", isOneDayButtonClicked)
            putBoolean("is_input_show", isInputShow)
            putString("button_text", buttonText)
            putString("sub_button_text", subButtonText)
            putInt("scroll_index", scrollIndex)
            putBoolean("isTracking", isTracking)
            putBoolean("isCoreEventTracking", isCoreEventTracking)
            putInt("subButtonClickCount", subButtonClickCount)
            putBoolean("isLastStopFromSub", isLastStopFromSub)
            putLong("core_duration", coreDuration.toMillis())
            putBoolean("isCoreDurationReset", isCoreDurationReset)

            if (startTimeTracking != null) {
                putString("startTimeTracking", startTimeTracking.toString())
            }

            if (currentEvent != null) {
                val eventJson = Json.encodeToString(Event.serializer(), currentEvent)
                putString("currentEvent", eventJson)
            }

            if (incompleteMainEvent != null) {
                val eventJson = Json.encodeToString(Event.serializer(), incompleteMainEvent)
                putString("incompleteMainEvent", eventJson)
            }

            val isSubEventType = eventType == EventType.SUB
            putBoolean("is_sub_event_type", isSubEventType)

            apply()
        }
    }

    fun getAllData(): SPData {
        val isOneDayButtonClicked = getIsOneDayButtonClicked()
        val isInputShow = getIsInputShow()
        val buttonText = getButtonText()
        val subButtonText = getSubButtonText()
        val isTracking = getIsTracking() // 内部使用
        val isCoreEventTracking = getIsCoreEventTracking() // 内部使用
        val coreDuration = getCoreDuration()
        val startTimeTracking = if (isCoreEventTracking) getStartTimeTracking() else null
        val currentEvent = if (isTracking) getCurrentEvent() else null
        val incompleteMainEvent =  if (isTracking) getIncompleteMainEvent() else null
        val scrollIndex = getScrollIndex()
        val subButtonClickCount = getSubButtonClickCount()
        val isSubEventType = getIsSubEventType()
        val isLastStopFromSub = getIsLastStopFromSub()
        val isCoreDurationReset = getIsCoreDurationReset()

        // 将获取的所有数据封装在 SharedPreferencesData 类的实例中
        return SPData(
            isOneDayButtonClicked,
            isInputShow,
            buttonText,
            subButtonText,
            isTracking,
            isCoreEventTracking,
            coreDuration,
            startTimeTracking,
            currentEvent,
            incompleteMainEvent,
            scrollIndex,
            subButtonClickCount,
            isSubEventType,
            isLastStopFromSub,
            isCoreDurationReset
        )
    }


    private fun getIsLastStopFromSub(): Boolean {
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

    private fun getCoreDuration(): Duration {
        val durationMillis = sharedPreferences.getLong("core_duration", -1L)
        return if (durationMillis != -1L) {
            Duration.ofMillis(durationMillis)
        } else Duration.ZERO
    }

    private fun getStartTimeTracking(): LocalDateTime? {
        val startTimeStr = sharedPreferences.getString("startTimeTracking", null)
        return startTimeStr?.let { LocalDateTime.parse(it) }
    }

    private fun getIsCoreEventTracking(): Boolean {
        return sharedPreferences.getBoolean("isCoreEventTracking", false)
    }

    private fun getIsCoreDurationReset(): Boolean {
        return sharedPreferences.getBoolean("isCoreDurationReset", false)
    }

}

