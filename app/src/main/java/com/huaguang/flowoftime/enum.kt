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
     * 没有下级的事件，于暂停的计算而言，直接使用 currentAcc 就可以了
     */
    fun isCurrent() = this == FOLLOW || this == SUBJECT_INSERT || this == STEP_INSERT
}

enum class TimeType {
    START,
    END
}

enum class InputIntent {
    RECORD,
    MODIFY,
}

enum class BlockType {
    RECORDING,
    DISPLAY,
}

enum class Mode {
    DISPLAY,
    RECORD,
}


enum class DashType {
    TAG,
    CATEGORY_ADD,
    CATEGORY_CHANGE,
    MIXED_ADD;

    fun isTag() = this == TAG

    fun isAdd() = this == CATEGORY_ADD || this == MIXED_ADD

    fun isCategory() = this == CATEGORY_ADD || this == CATEGORY_CHANGE
}



