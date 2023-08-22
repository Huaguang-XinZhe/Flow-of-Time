package com.huaguang.flowoftime.wait

import android.annotation.SuppressLint
import androidx.compose.runtime.mutableStateOf

interface SelectionHandler {
    fun handleSelection(id: Long, map: MutableMap<Long, Boolean>)
}

class CancelSelectionHandler : SelectionHandler {
    override fun handleSelection(id: Long, map: MutableMap<Long, Boolean>) {
        map[id] = false
    }
}

class ClearSelectionHandler : SelectionHandler {
    override fun handleSelection(id: Long, map: MutableMap<Long, Boolean>) {
        map.clear()
    }
}

class SelectionTracker(private val selectionHandler: SelectionHandler) {
    @SuppressLint("MutableCollectionMutableState")
    private val selectedIds = mutableStateOf(mutableMapOf<Long, Boolean>())

    private val newMap
        get() = selectedIds.value.toMutableMap()

    fun isSelected(id: Long): Boolean = selectedIds.value[id] ?: false

    fun toggleSelection(id: Long) {
        newMap[id] = !isSelected(id)
        selectedIds.value = newMap
    }

    fun handleSelection(id: Long) {
        selectionHandler.handleSelection(id, newMap)
        selectedIds.value = newMap
    }
}
