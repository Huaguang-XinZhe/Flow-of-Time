package com.huaguang.flowoftime.data.models.tables

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity("inspirations")
data class Inspiration(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val date: LocalDate,
    val category: String? = null, // 默认无类属
)
