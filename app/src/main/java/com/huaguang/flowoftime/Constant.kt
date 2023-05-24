package com.huaguang.flowoftime

import java.time.Duration

val names = listOf(
    "时光流程序开发",
    "吃什么程序开发",
    "compose学习",
    "Compose 学习"
)

// TODO: 允许集中自定义
val minutesThreshold: Duration = Duration.ofMinutes(20L)
val hourThreshold: Duration = Duration.ofHours(8)
val hourThreshold2: Duration = Duration.ofHours(5)

enum class EventType {
    MAIN,
    SUB
}
