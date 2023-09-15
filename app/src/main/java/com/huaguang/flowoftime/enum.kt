package com.huaguang.flowoftime


enum class EventType {
    SUBJECT, // 可以有子事件：步骤、插入、伴随
    STEP, // 也可以有子事件：插入（只能在主题事件之下）
    FOLLOW, // 不能有子事件（一般在主题事件之下）
    SUBJECT_INSERT,
    STEP_INSERT; // 不能有子事件（可以在步骤和主题之下）

    fun isExpandable() = this == SUBJECT || this == STEP

    fun isInsert() = this == SUBJECT_INSERT || this == STEP_INSERT

    /**
     * 只允许主题插入和步骤插入调用此方法，这是在编码是调用的，一般不会出错，即结果不会是空字符串。
     */
    fun endName(): String {
        return if (this == SUBJECT_INSERT) "插入结束" else if (this == STEP_INSERT) "step 插入结束" else ""
    }
}

enum class Action(val value: Int) {
    SUBJECT_START(0),
    SUBJECT_END(1),
    STEP_START(0),
    STEP_END(1),
    SUBJECT_INSERT_START(0),
    SUBJECT_INSERT_END(1),
    STEP_INSERT_START(0),
    STEP_INSERT_END(1),
    FOLLOW_START(0),
    FOLLOW_END(1);

    fun isStart() = this.value == 0

    fun isEnd() = this.value == 1
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

