package com.huaguang.flowoftime.ui.pages.time_record.core_fab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.custom_interface.ButtonsStateControl
import com.huaguang.flowoftime.custom_interface.EventControl
import com.huaguang.flowoftime.data.sources.SPHelper
import com.huaguang.flowoftime.ui.state.InputState
import com.huaguang.flowoftime.ui.state.ItemState
import com.huaguang.flowoftime.ui.state.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrentCoreViewModel @Inject constructor(
    val sharedState: SharedState,
    val inputState: InputState,
    val spHelper: SPHelper,
) : ViewModel() {
    var coreName = ""
    private var confirmThenStart = false

    fun coreButtonNotShow(): Boolean{
        val subTiming = sharedState.cursorType.value?.let {
            it != EventType.SUBJECT
        } ?: false // 没有事件正在计时，也就意味着没有子项正在计时，为 false

        return inputState.show.value || subTiming
    }

    fun onCoreFloatingButtonClick(
        eventControl: EventControl,
        buttonsStateControl: ButtonsStateControl,
        displayItemState: ItemState,
        recordingItemState: ItemState,
    ) {
        viewModelScope.launch {
            coreName = spHelper.getCurrentCoreEventName(coreName)

            if (coreName.isEmpty()) { // 在最开始的时候，SP 中没有值，coreName 仍有可能为空，这是就弹窗请用户设置，然后再开始事件
                sharedState.apply {
                    coreInputShow.value = true
                    toastMessage.value = "请预先设置当前核心（名称）"
                }
                confirmThenStart = true // 设置好点击确认就马上开启一个新事件

                return@launch
            }

            val type = if (hasSubjectExist()) EventType.FOLLOW else EventType.SUBJECT

            eventControl.startEvent(
                name = coreName,
                eventType = type
            )

            if (hasSubjectExist()) {
                buttonsStateControl.followTiming() // 切换到 ”伴随结束“ 的按钮状态
            } else {
                buttonsStateControl.subjectTiming() // 切换到 “主题结束” 的按钮状态
                buttonsStateControl.resetItemState(displayItemState, recordingItemState)
            }
        }
    }

    fun onCoreFloatingButtonLongClick() {
        coreName = spHelper.getCurrentCoreEventName(coreName)
        sharedState.coreInputShow.value = true // 显示名称输入 Dialog
    }

    fun onCoreNameDialogDismiss() {
        sharedState.coreInputShow.value = false
    }


    fun onCoreNameDialogConfirm(
        newText: String,
        eventControl: EventControl,
        buttonsStateControl: ButtonsStateControl,
        displayItemState: ItemState,
        recordingItemState: ItemState
    ) {
        onCoreNameDialogDismiss()
        if (newText.isEmpty() && newText == coreName) return

        coreName = newText // 必须同时更新内存中的 coreName
        spHelper.saveCurrentCoreEventName(newText)

        if (confirmThenStart) { // 最开始的时候，设置完就开启新事件
            onCoreFloatingButtonClick(
                eventControl,
                buttonsStateControl,
                displayItemState,
                recordingItemState
            )
            confirmThenStart = false // 重置，以防止在本次应用周期内的下次修改再次开启
        }
    }

    private fun hasSubjectExist() = sharedState.cursorType.value != null

}