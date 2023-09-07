package com.huaguang.flowoftime.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.huaguang.flowoftime.other.DurationSerializer
import com.huaguang.flowoftime.other.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.LocalDate

@Serializable
@Entity(tableName = "date_durations")
data class DateDuration(
    @PrimaryKey
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,

    @Serializable(with = DurationSerializer::class)
    var duration: Duration,

    var durationStr: String,
)

