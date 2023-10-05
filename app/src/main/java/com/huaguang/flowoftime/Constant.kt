package com.huaguang.flowoftime

import java.time.Duration

// TODO: 关键词应该允许自由编辑配置
val coreEventKeyWords = listOf(
    "开发",
    "程序开发",
    "compose",
    "学习",
    "修复",
    "bug",
    "Android",
    "扔物线",
    "时光流",
    "吃什么",
)

val sleepNames = listOf("睡", "睡觉", "晚睡")

// TODO: 允许集中自定义
// 用于判断是否取消闹钟的时间阈值。如果剩余时间大于此阈值，将取消闹钟，等待更适合的时间重设。
val ALARM_CANCELLATION_THRESHOLD: Duration = Duration.ofMinutes(20L)

// 对某一关注事务的持续时间的阈值。这是每天至少需要达到的持续时间。当事务的持续时间达到此阈值时，将自动发出提醒。
val FOCUS_EVENT_DURATION_THRESHOLD: Duration = Duration.ofHours(8)
// 设置闹钟的时间阈值。当关注事务的剩余可用时间少于此阈值时，将设置闹钟。
// 这是为了减少设置闹钟的频率，因为大多数事务的持续时间都不会超过此阈值。
val ALARM_SETTING_THRESHOLD: Duration = Duration.ofHours(5)

const val separator = "^, ^" // 分隔符



