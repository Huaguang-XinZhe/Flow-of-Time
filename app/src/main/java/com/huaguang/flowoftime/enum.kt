package com.huaguang.flowoftime

enum class EventStatus(val value: Int) {
    NO_EVENT_IN_PROGRESS(0),
    ONLY_MAIN_EVENT_IN_PROGRESS(1),
    MAIN_AND_SUB_EVENT_IN_PROGRESS(2);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}

enum class EventType {
    SUBJECT, // 可以有子事件：步骤、插入、伴随
    STEP, // 也可以有子事件：插入
    FOLLOW, // 不能有子事件
    INSERT; // 不能有子事件

    fun isExpandable() = this == SUBJECT || this == STEP
}

enum class TimeType {
    START,
    END
}

enum class InputIntent {
    RECORD,
    MODIFY,
}

enum class ItemType {
    DISPLAY,
    RECORD,
}