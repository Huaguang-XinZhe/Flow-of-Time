package com.huaguang.flowoftime.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.huaguang.flowoftime.EventType
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) var id: Long = 0L,
    var name: String = "",

    var startTime: LocalDateTime,

    var endTime: LocalDateTime? = null,

    var duration: Duration? = null,

    var withContent: Boolean = false,

    var pauseInterval: Int = 0, // 当前事件进行过程中所暂停的时间，原则上不许暂停，为应对现实变化而设置。插入事项不允许有暂停间隔（限制按钮点击）

    var reasonsForDistraction: String? = null, // 分心的理由和时长，有理由就不为 null，其值累加到 pauseInterval 中，但该理由中也保留值。

    val type: EventType,

    var category: String? = null, // 除主题事件外无类属

    val tags: List<String>? = null, // 除主题事件外无标签

    // 为了辅助查询————————————————————————————————————————————
    var eventDate: LocalDate? = null, // 用于存储事件发生的日期

    var parentEventId: Long? = null
)

