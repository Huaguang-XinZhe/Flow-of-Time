package com.huaguang.flowoftime.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.huaguang.flowoftime.data.models.tables.IconMapping
import kotlinx.coroutines.flow.Flow

@Dao
interface IconMappingDao {

    @Query("SELECT * FROM iconMapping WHERE category = :className")
    fun getMappingForClass(className: String): Flow<IconMapping>

    @Query("SELECT * FROM iconMapping WHERE category IS NOT NULL")
    fun getNotNullMapping(): List<IconMapping>

}