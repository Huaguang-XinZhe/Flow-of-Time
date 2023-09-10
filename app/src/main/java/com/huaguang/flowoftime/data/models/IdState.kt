package com.huaguang.flowoftime.data.models

import androidx.compose.runtime.MutableState
import com.huaguang.flowoftime.data.sources.SPHelper

data class IdState(
    val current: MutableState<Long>,
    val subject: MutableState<Long>,
    val step: MutableState<Long>,
) {
    companion object {
        fun initialValue(spHelper: SPHelper) = spHelper.getIdState()
    }
}
