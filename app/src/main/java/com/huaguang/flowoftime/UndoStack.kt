package com.huaguang.flowoftime

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.huaguang.flowoftime.data.models.Operation
import java.util.Stack

class UndoStack {

    private val undoStack = Stack<Operation>()
    private val maxUndoSteps = 3

    var canUndo by mutableStateOf(false)
        private set

    fun addState(state: Operation) {
        if (undoStack.size == maxUndoSteps) {
            undoStack.removeAt(0)
        }
        undoStack.push(state)
        updateCanUndo()
    }

    fun undo(): Operation? {
        return if (undoStack.isNotEmpty()) {
            undoStack.pop()
        } else {
            null
        }.also {
            updateCanUndo()
        }
    }

    /**
     * 在使用 Compose 的 mutableStateOf 时，你不需要手动检查新值和旧值是否不同。
     * Compose 会自动处理这个，只有当值实际更改时才会触发重新组合。
     */
    private fun updateCanUndo() {
        canUndo = undoStack.isNotEmpty()
    }
}


