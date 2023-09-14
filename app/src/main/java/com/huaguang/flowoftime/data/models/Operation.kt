package com.huaguang.flowoftime.data.models

import com.huaguang.flowoftime.Action

data class Operation(
    val action: Action, // 操作的类型
    val eventId: Long, // 影响的数据行 id
    val pauseInterval: Int = 0, // 结束前计算得到的总的暂停间隔
    val immutableIdState: ImmutableIdState? = null, // 开始事件时会更改 idState，要记下来，但必须使用不可变对象
)

data class ImmutableIdState(
    val current: Long,
    val subject: Long,
    val step: Long,
)