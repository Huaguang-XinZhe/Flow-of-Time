package com.huaguang.flowoftime.ui.state

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class CategoryLabelState(
    var id: Long = 0,
    var category: String = "null",
    val show: MutableState<Boolean> = mutableStateOf(false)
)

