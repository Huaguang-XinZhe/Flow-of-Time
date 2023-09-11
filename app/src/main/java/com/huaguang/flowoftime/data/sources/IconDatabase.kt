package com.huaguang.flowoftime.data.sources

import androidx.room.Database
import androidx.room.RoomDatabase
import com.huaguang.flowoftime.data.dao.IconMappingDao
import com.huaguang.flowoftime.data.models.tables.IconMapping

@Database(entities = [IconMapping::class], version = 1, exportSchema = false)
abstract class IconDatabase : RoomDatabase() {

    abstract fun iconMappingDao(): IconMappingDao
}