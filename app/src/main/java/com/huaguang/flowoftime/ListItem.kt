package com.huaguang.flowoftime

import com.huaguang.flowoftime.data.models.Event
import java.time.LocalDate

sealed class ListItem {
    data class DateItem(val value: LocalDate): ListItem()
    data class MainItem(val event: Event): ListItem()
    data class SubItem(val event: Event): ListItem()
    data class Interval(val value: Int): ListItem()
}

