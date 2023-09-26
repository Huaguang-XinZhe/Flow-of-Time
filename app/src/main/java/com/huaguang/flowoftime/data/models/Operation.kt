package com.huaguang.flowoftime.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Operation(
    val action: Action, // 操作的类型
    val eventId: Long, // 影响的数据行 id
    val pauseInterval: Int = 0, // 结束前计算得到的总的暂停间隔
    val immutableIdState: ImmutableIdState? = null, // 开始事件时会更改 idState，要记下来，但必须使用不可变对象
)

@Serializable
data class ImmutableIdState(
    val current: Long,
    val subject: Long,
    val step: Long,
)

@Serializable
enum class Action(val value: Int) {
    SUBJECT_START(0),
    SUBJECT_END(1), // 关键，只有这个撤销会影响到统计数据
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