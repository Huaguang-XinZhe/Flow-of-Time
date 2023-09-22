package com.huaguang.flowoftime.data.sources

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import com.huaguang.flowoftime.EventType
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

    fun getCursorType(): MutableState<EventType?> {
        val typeName = sp.getString("cursor_type", null) ?: return mutableStateOf(null)
        return mutableStateOf(EventType.valueOf(typeName))
    }

    fun getPauseState(): PauseState {
        val startLong = sp.getLong("start_second", -1L)
        val start = if (startLong != -1L) {
            val instant = Instant.ofEpochSecond(startLong)
            LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        } else null

        return PauseState(
            start = mutableStateOf(start),
            acc = mutableIntStateOf(sp.getInt("acc", 0)),
            subjectAcc = mutableIntStateOf(sp.getInt("subject_acc", 0)),
            stepAcc = mutableIntStateOf(sp.getInt("step_acc", 0)),
            currentAcc = mutableIntStateOf(sp.getInt("current_acc", 0))
        )
    }

    fun getSerializedData(): String? {
        return sp.getString("undo_stack", null) // 为 null 就不反序列化了
    }

    fun saveState(
        idState: IdState,
        buttonsState: ButtonsState,
        pauseState: PauseState,
        cursorType: MutableState<EventType?>,
        undoStackJson: String,
    ) {
        sp.edit().apply {
            saveIdState(idState)
            saveButtonsState(buttonsState)
            savePauseState(pauseState)
            saveCursorType(cursorType)
            saveUndoStackJson(undoStackJson)
            apply()
        }
    }

    fun getButtonsState(): ButtonsState {
        return ButtonsState(
            mainText = mutableStateOf(sp.getString("mainText", "开始") ?: "开始"),
            mainShow = mutableStateOf(sp.getBoolean("mainShow", true)),
            subText = mutableStateOf(sp.getString("subText", "插入") ?: "插入"),
            subShow = mutableStateOf(sp.getBoolean("subShow", false)),
        )
    }


    fun getIdState() = IdState(
        current = mutableLongStateOf(sp.getLong("current", 0L)),
        subject = mutableLongStateOf(sp.getLong("subject", 0L)),
        step = mutableLongStateOf(sp.getLong("step", 0L)),
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

    private fun SharedPreferences.Editor.saveUndoStackJson(value: String) {
        this.putString("undo_stack", value)
    }

    private fun SharedPreferences.Editor.saveButtonsState(buttonsState: ButtonsState) {
        with(this) {
            putString("mainText", buttonsState.mainText.value)
            putBoolean("mainShow", buttonsState.mainShow.value)
            putString("subText", buttonsState.subText.value)
            putBoolean("subShow", buttonsState.subShow.value)
        }
    }

    private fun SharedPreferences.Editor.savePauseState(pauseState: PauseState) {
        // 注意！这里必须处理 start 的值可能为 null 的情况，要不然每次点开应用都会重新加载，但就是不崩溃！！！
        // 不，可能已经崩溃了，但因为刚好切换应用（退出前台），所以看不着。
        val epochSecond = pauseState.start.value?.let {
            val zonedDateTime = ZonedDateTime.of(pauseState.start.value, ZoneId.systemDefault())
            zonedDateTime.toEpochSecond()
        } ?: -1L

        with(this) {
            putLong("start_second", epochSecond)
            putInt("acc", pauseState.acc.value)
            putInt("subject_acc", pauseState.subjectAcc.value)
            putInt("step_acc", pauseState.stepAcc.value)
            putInt("current_acc", pauseState.currentAcc.value)
        }
    }

    private fun SharedPreferences.Editor.saveCursorType(cursorType: MutableState<EventType?>) {
        this.putString("cursor_type", cursorType.value?.name)
    }

}


