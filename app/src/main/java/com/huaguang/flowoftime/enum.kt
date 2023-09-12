package com.huaguang.flowoftime


enum class EventType {
    SUBJECT, // 可以有子事件：步骤、插入、伴随
    STEP, // 也可以有子事件：插入（只能在主题事件之下）
    FOLLOW, // 不能有子事件（一般在主题事件之下）
    INSERT; // 不能有子事件（可以在步骤和主题之下）

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