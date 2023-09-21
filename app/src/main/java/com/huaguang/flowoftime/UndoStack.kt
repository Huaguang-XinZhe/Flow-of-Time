package com.huaguang.flowoftime

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.huaguang.flowoftime.data.models.Operation
import com.huaguang.flowoftime.data.sources.SPHelper
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.util.Stack

class UndoStack(spHelper: SPHelper) {

    private val undoStack = Stack<Operation>()
    private val maxUndoSteps = 3

    var undoShow by mutableStateOf(false)
        private set

    init {
        spHelper.getSerializedData()?.let { deserialize(it) }
    }

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
        return if (undoStack.isNotEmpty()) {
//            setUndoShow() // 不能放在这里，在 pop 之前检查栈的大小是不会变化的，必须放在 pop 之后。
            undoStack.pop()
        } else {
            null
        }.also {
            setUndoShow()
        }
    }

    fun serialize(): String { // 必须使用 ListSerializer，否则要求 value 为 Operation 类型
        return Json.encodeToString(ListSerializer(Operation.serializer()), undoStack.toList())
    }

    private fun deserialize(serializedData: String) {
        undoStack.addAll(Json.decodeFromString<List<Operation>>(serializedData))
        setUndoShow()
    }

    /**
     * 在使用 Compose 的 mutableStateOf 时，你不需要手动检查新值和旧值是否不同。
     * Compose 会自动处理这个，只有当值实际更改时才会触发重新组合。
     */
    private fun setUndoShow() {
        undoShow = undoStack.isNotEmpty() // 只有撤销栈非空才显示按钮
    }
}


