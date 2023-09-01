package com.huaguang.flowoftime.wait

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CategoryIconDao {
    @Query("SELECT * FROM CategoryIcon WHERE categoryName = :name")
    fun getCategoryIconByName(name: String): CategoryIcon?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg categoryIcons: CategoryIcon)

    @Delete
    fun delete(categoryIcon: CategoryIcon)
}
