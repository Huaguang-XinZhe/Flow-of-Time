package com.huaguang.flowoftime

import java.util.Stack

class UndoStack<T> {

    private val undoStack = Stack<T>()
    private val maxUndoSteps = 3

    fun addState(state: T) {
        if (undoStack.size == maxUndoSteps) {
            undoStack.removeAt(0) // 移除最旧的状态来为新状态腾出空间
        }
        undoStack.push(state)
    }

    fun undo(): T? {
        return if (undoStack.isNotEmpty()) {
            undoStack.pop()
        } else {
            null
        }
    }

    fun canUndo(): Boolean {
        return undoStack.isNotEmpty()
    }
}

fun main() {
    val undoStack = UndoStack<String>()

    undoStack.addState("State1")
    undoStack.addState("State2")
    undoStack.addState("State3")
    undoStack.addState("State4")

    println(undoStack.undo()) // 输出: State4
    println(undoStack.undo()) // 输出: State3
    println(undoStack.undo()) // 输出: State2
    println(undoStack.undo()) // 输出: null
}
