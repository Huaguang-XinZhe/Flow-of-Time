package com.huaguang.flowoftime.data.models.tables

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Duration
import java.time.LocalDate

@Entity(tableName = "daily_statistics")
data class DailyStatistics(
    @PrimaryKey(autoGenerate = true) var id: Long = 0L,
    val date: LocalDate, // 无需更新
    val category: String?, // 无需更新，也存储未类属的
    var totalDuration: Duration,
)
