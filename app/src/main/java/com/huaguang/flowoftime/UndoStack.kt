package com.huaguang.flowoftime

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.data.models.Operation
import java.util.Stack

class UndoStack {

    private val undoStack = Stack<Operation>()
    private val maxUndoSteps = 3

    var undoShow by mutableStateOf(false)
        private set

    fun addState(state: Operation) {
        if (undoStack.size == maxUndoSteps) {
            undoStack.removeAt(0)
        }
//        else {
//            setUndoShow() // 不能放在这里，在 push 之前检查栈的大小也是不会变化的，必须放在 push 之后。
//        }
        undoStack.push(state)
        setUndoShow()
    }

    fun undo(): Operation? {
        RDALogger.info("撤销按钮点击！")
        return if (undoStack.isNotEmpty()) {
//            setUndoShow() // 不能放在这里，在 pop 之前检查栈的大小是不会变化的，必须放在 pop 之后。
            undoStack.pop()
        } else {
            null
        }.also {
            setUndoShow()
        }
    }

    /**
     * 在使用 Compose 的 mutableStateOf 时，你不需要手动检查新值和旧值是否不同。
     * Compose 会自动处理这个，只有当值实际更改时才会触发重新组合。
     */
    private fun setUndoShow() {
        undoShow = undoStack.isNotEmpty() // 只有撤销栈非空才显示按钮
        undoStack.forEach { operation ->
            RDALogger.info("operation = $operation")
        }
    }
}


