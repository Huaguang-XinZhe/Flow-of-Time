package com.huaguang.flowoftime.data.models

import com.huaguang.flowoftime.Action

data class Operation(
    val action: Action, // 操作的类型
    val eventId: Long, // 影响的数据行 id
)
