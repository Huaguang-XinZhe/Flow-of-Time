package com.huaguang.flowoftime.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.huaguang.flowoftime.data.models.db_returns.IdText
import com.huaguang.flowoftime.data.models.tables.Inspiration
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface InspirationDao {
    // 插入一条灵感记录
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspiration(inspiration: Inspiration): Long

    // 更新一条灵感记录
    @Update
    suspend fun updateInspiration(inspiration: Inspiration)

    // 根据ID删除一条灵感记录
    @Query("DELETE FROM inspirations WHERE id = :id")
    suspend fun deleteInspirationById(id: Long)

    // 查询所有灵感记录
    @Query("SELECT * FROM inspirations")
    suspend fun getAllInspirations(): List<Inspiration>

    @Query("SELECT * FROM inspirations ORDER BY id DESC") // 降序排列
    fun getAllInspirationsFlow(): Flow<List<Inspiration>>

    // 根据类属查询灵感记录
    @Query("SELECT * FROM inspirations WHERE category = :category")
    suspend fun getInspirationsByCategory(category: String): List<Inspiration>

    // 根据日期查询灵感记录
    @Query("SELECT * FROM inspirations WHERE date BETWEEN :fromDate AND :toDate")
    suspend fun getInspirationsByDateRange(fromDate: LocalDate, toDate: LocalDate): List<Inspiration>

    // 根据ID查询单个灵感记录
    @Query("SELECT * FROM inspirations WHERE id = :id")
    suspend fun getInspirationById(id: Int): Inspiration?

    @Query("SELECT id as maxId, text FROM inspirations WHERE id = (SELECT MAX(id) FROM inspirations)")
    suspend fun getLastIdText(): IdText

    @Query("UPDATE inspirations SET text = :newText WHERE id = :id")
    suspend fun updateTextById(id: Long, newText: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(inspirations: List<Inspiration>)
}