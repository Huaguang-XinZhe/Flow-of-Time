package com.huaguang.flowoftime

import com.huaguang.flowoftime.data.models.Event

sealed class ListItem {
    data class MainItem(val event: Event): ListItem()
    data class SubItem(val event: Event): ListItem()
}

