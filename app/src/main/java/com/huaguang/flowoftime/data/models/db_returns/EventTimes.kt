package com.huaguang.flowoftime.data.models.db_returns

import java.time.LocalDateTime

/**
 * 这是存储多列查询结果的数据类，不满足范围的部分以 null 代替
 */
data class EventTimes(
    val startTime: LocalDateTime?, // 可能为 null
    val endTime: LocalDateTime?    // 可能为 null
)


