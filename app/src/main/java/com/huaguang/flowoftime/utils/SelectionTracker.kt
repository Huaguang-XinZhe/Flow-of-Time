package com.huaguang.flowoftime.utils

import android.annotation.SuppressLint
import androidx.compose.runtime.mutableStateOf

class SelectionTracker {
    @SuppressLint("MutableCollectionMutableState")
    private val selectedIds = mutableStateOf(mutableMapOf<Long, Boolean>())

//    private val newMap
//        get() = selectedIds.value.toMutableMap()

    fun isSelected(id: Long): Boolean = selectedIds.value[id] ?: false

    fun toggleSelection(id: Long) {
        val newMap = selectedIds.value.toMutableMap()
        newMap[id] = !isSelected(id)
        selectedIds.value = newMap
    }

    fun cancelSelection(id: Long) {
        val newMap = selectedIds.value.toMutableMap()
        newMap[id] = false
        selectedIds.value = newMap
    }

    fun clearSelection() {
        selectedIds.value = mutableMapOf()
    }

}
