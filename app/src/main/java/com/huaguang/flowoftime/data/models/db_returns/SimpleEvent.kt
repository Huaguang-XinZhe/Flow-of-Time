package com.huaguang.flowoftime.data.models.db_returns

annotation class CsvOrder(val order: Int)


/**
 * 简化版事件类，用来接收昨日的数据查询结果，以便接下来导出为 CSV 文件
 * 奇怪，怎么真机运行的时候又不按顺序了？
 */
data class SimpleEvent(
    @CsvOrder(1) val id: Long,
    @CsvOrder(2) val name: String,
    @CsvOrder(3) val duration: String,
    @CsvOrder(4) val category: String?,
    @CsvOrder(5) val tags: List<String>?,
    @CsvOrder(6) val type: String,
    @CsvOrder(7) val eventDate: String,
    @CsvOrder(8) val parentEventId: Long?,
)

