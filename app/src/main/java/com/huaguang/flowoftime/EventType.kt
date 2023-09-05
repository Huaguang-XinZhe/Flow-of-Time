package com.huaguang.flowoftime

enum class EventType {
    SUBJECT,
    STEP,
    FOLLOW,
    INSERT;

    fun isExpandable() = this == SUBJECT || this == STEP
}