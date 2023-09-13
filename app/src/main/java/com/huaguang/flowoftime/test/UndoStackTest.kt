package com.huaguang.flowoftime.test
import com.huaguang.flowoftime.data.models.Operation
import java.util.Stack
import kotlin.properties.Delegates


/**
 * 你可以使用一个属性来观察undoStack的大小，并在其更改时通知UI。Kotlin提供了一个叫做Delegates.observable的委托，可以用来观察属性的更改。
 * 在这个版本中，canUndo现在是一个可观察的属性。每当undoStack的大小更改时，它都会更新canUndo属性的值，并通过onUndoStackChanged回调通知UI。
 */
class UndoStack(private val onUndoStackChanged: (Boolean) -> Unit) {

    private val undoStack = Stack<Operation>()
    private val maxUndoSteps = 3

    var canUndo: Boolean by Delegates.observable(false) { _, _, newValue ->
        onUndoStackChanged(newValue)
    }

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
     * 为了确保onUndoStackChanged只在canUndo的值实际更改时被调用，我们可以在updateCanUndo方法中添加一个检查来避免不必要的回调调用。
     * 在我之前提供的方案中，每次调用addState或undo方法时，都会调用updateCanUndo方法来更新canUndo属性的值，
     * 这可能会触发onUndoStackChanged回调，即使canUndo的值实际上没有更改。
     */
    private fun updateCanUndo() {
        val newCanUndo = undoStack.isNotEmpty()
        if (newCanUndo != canUndo) {
            canUndo = newCanUndo
        }
    }

}

val undoStack = UndoStack(onUndoStackChanged = { canUndo ->
    // 更新UI以反映是否可以撤销
    if (canUndo) {
        println("Can undo")
    } else {
        println("Cannot undo")
    }
})
