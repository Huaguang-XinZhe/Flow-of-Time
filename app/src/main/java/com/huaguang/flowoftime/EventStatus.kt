package com.huaguang.flowoftime

enum class EventStatus(val value: Int) {
    NO_EVENT_IN_PROGRESS(0),
    ONLY_MAIN_EVENT_IN_PROGRESS(1),
    MAIN_AND_SUB_EVENT_IN_PROGRESS(2);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}