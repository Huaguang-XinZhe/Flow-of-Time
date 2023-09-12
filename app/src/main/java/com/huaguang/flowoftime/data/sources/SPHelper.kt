package com.huaguang.flowoftime.data.sources

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import com.huaguang.flowoftime.ui.state.ButtonsState
import com.huaguang.flowoftime.ui.state.IdState
import com.huaguang.flowoftime.ui.state.PauseState
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

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

    fun getPauseState(): PauseState {
        val startLong = sp.getLong("start_second", -1L)
        val start = if (startLong != -1L) {
            val instant = Instant.ofEpochSecond(startLong)
            LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        } else null

        return PauseState(
            start = mutableStateOf(start),
            acc = mutableStateOf(sp.getInt("acc", 0))
        )
    }

    fun getStepTiming(): Boolean {
        return sp.getBoolean("step_timing", false)
    }

    fun saveState(
        idState: IdState,
        buttonsState: ButtonsState,
        pauseState: PauseState,
        stepTiming: Boolean
    ) {
        sp.edit().apply {
            saveIdState(idState)
            saveButtonsState(buttonsState)
            savePauseState(pauseState)
            saveStepTiming(stepTiming)
            apply()
        }
    }

    fun getButtonsState(): ButtonsState {
        return ButtonsState(
            mainText = mutableStateOf(sp.getString("mainText", "开始") ?: "开始"),
            mainShow = mutableStateOf(sp.getBoolean("mainShow", true)),
            subText = mutableStateOf(sp.getString("subText", "插入") ?: "插入"),
            subShow = mutableStateOf(sp.getBoolean("subShow", false)),
            undoShow = mutableStateOf(sp.getBoolean("undoShow", false))
        )
    }


    fun getIdState() = IdState(
        current = mutableStateOf(sp.getLong("current", 0L)),
        subject = mutableStateOf(sp.getLong("subject", 0L)),
        step = mutableStateOf(sp.getLong("step", 0L))
    )

    fun saveRingVolume(value: Int) {
        sp.edit().putInt("ring_volume", value).apply()
    }

    fun getRingVolume(): Int {
        return sp.getInt("ring_volume", 0)
    }

    fun getCurrentCoreEventName(coreName: String): String {
        return coreName.ifEmpty { // 如果为空就执行下面的语句，不为空就返回 coreName
            sp.getString("current_core_event_name", "") ?: ""
        }
    }

    fun saveCurrentCoreEventName(value: String) {
        sp.edit().putString("current_core_event_name", value).apply()
    }

    private fun SharedPreferences.Editor.saveIdState(idState: IdState) {
        with(this) {
            putLong("current", idState.current.value)
            putLong("subject", idState.subject.value)
            putLong("step", idState.step.value)
        }
    }

    private fun SharedPreferences.Editor.saveButtonsState(buttonsState: ButtonsState) {
        with(this) {
            putString("mainText", buttonsState.mainText.value)
            putBoolean("mainShow", buttonsState.mainShow.value)
            putString("subText", buttonsState.subText.value)
            putBoolean("subShow", buttonsState.subShow.value)
            putBoolean("undoShow", buttonsState.undoShow.value)
        }
    }

    private fun SharedPreferences.Editor.saveStepTiming(value: Boolean) {
        this.putBoolean("step_timing", value)
    }

    private fun SharedPreferences.Editor.savePauseState(pauseState: PauseState) {
        val zonedDateTime = ZonedDateTime.of(pauseState.start.value, ZoneId.systemDefault())
        val epochSecond = zonedDateTime.toEpochSecond()

        with(this) {
            putLong("start_second", epochSecond)
            putInt("acc", pauseState.acc.value)
        }
    }

}


