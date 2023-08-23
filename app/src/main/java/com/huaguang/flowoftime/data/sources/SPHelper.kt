package com.huaguang.flowoftime.data.sources

import android.content.Context
import com.huaguang.flowoftime.EventStatus
import com.huaguang.flowoftime.data.models.Event
import kotlinx.serialization.json.Json
import java.time.Duration

/**
 * 实现了单例的 SharedPreferences 帮助类
 */
class SPHelper private constructor(context: Context) {

    private val sp = context.getSharedPreferences("sp", Context.MODE_PRIVATE)

    companion object {
        // SPHelper 类维护的一个自身类型的静态实例
        private var instance: SPHelper? = null

        // 获取自身单例的方法
        fun getInstance(context: Context): SPHelper {
            if (instance == null) {
                instance = SPHelper(context)
            }
            return instance as SPHelper
        }
    }

    fun savePauseInterval(value: Int) {
        val accValue = getPauseInterval() + value
        sp.edit().putInt("pause_interval", accValue).apply()
    }

    fun getPauseInterval(): Int {
        return sp.getInt("pause_interval", 0)
    }

    fun resetPauseInterval() {
        sp.edit().putInt("pause_interval", 0).apply()
    }

    fun saveRingVolume(value: Int) {
        sp.edit().putInt("ring_volume", value).apply()
    }

    fun getRingVolume(): Int {
        return sp.getInt("ring_volume", 0)
    }

    fun saveState(
        isOneDayButtonClicked: Boolean,
        isInputShow: Boolean,
        buttonText: String,
        subButtonText: String,
        scrollIndex: Int,
        coreDuration: Duration,
        currentEvent: Event?,
        eventStatus: EventStatus,
    ) {
        sp.edit().apply {
            putBoolean("IS_ONE_DAY_BUTTON_CLICKED", isOneDayButtonClicked)
            putBoolean("is_input_show", isInputShow)
            putString("button_text", buttonText)
            putString("sub_button_text", subButtonText)
            putInt("scroll_index", scrollIndex)
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

        // 将获取的所有数据封装在 spData 类的实例中
        return SPData(
            isOneDayButtonClicked,
            isInputShow,
            buttonText,
            subButtonText,
            coreDuration,
            eventStatus,
            currentEvent,
            scrollIndex,
        )
    }

    private fun getEventStatus(): EventStatus {
        val eventStatusValue = sp.getInt("event_status_value", 0)
        return EventStatus.fromInt(eventStatusValue)
    }

    private fun getCurrentEvent(): Event? {
        val eventJson = sp.getString("currentEvent", null)
            ?: return null
        return Json.decodeFromString<Event>(eventJson)
    }

    private fun getIsOneDayButtonClicked(): Boolean {
        return sp.getBoolean("IS_ONE_DAY_BUTTON_CLICKED", false)
    }

    private fun getIsInputShow(): Boolean {
        return sp.getBoolean("is_input_show", false)
    }

    private fun getButtonText(): String {
        return sp.getString("button_text", "开始") ?: "开始"
    }

    private fun getSubButtonText(): String {
        return sp.getString("sub_button_text", "插入") ?: "插入"
    }

    private fun getScrollIndex(): Int {
        return sp.getInt("scroll_index", -1)
    }

    private fun getCoreDuration(): Duration {
        val durationMillis = sp.getLong("core_duration", -1L)
        return if (durationMillis != -1L) {
            Duration.ofMillis(durationMillis)
        } else Duration.ZERO
    }

}

data class SPData(
    val isOneDayButtonClicked: Boolean,
    val isInputShow: Boolean,
    val buttonText: String,
    val subButtonText: String,
    val coreDuration: Duration,
    val eventStatus: EventStatus,
    val currentEvent: Event?,
    val scrollIndex: Int,
)
