package com.huaguang.flowoftime.data.models

import com.huaguang.flowoftime.Action

data class Operation(
    val action: Action, // 操作的类型
    val eventId: Long, // 影响的数据行 id
    val pauseInterval: Int = 0, // 结束前计算得到的总的暂停间隔
)
