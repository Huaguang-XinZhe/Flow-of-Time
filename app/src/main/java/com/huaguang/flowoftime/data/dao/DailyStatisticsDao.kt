package com.huaguang.flowoftime.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.huaguang.flowoftime.data.models.db_returns.DateDuration
import com.huaguang.flowoftime.data.models.tables.DailyStatistics
import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.time.LocalDate

@Dao
interface DailyStatisticsDao {

    @Query("""
        SELECT * FROM daily_statistics 
        WHERE date = :date 
        AND ((category = :category AND :category IS NOT NULL) OR (category IS NULL AND :category IS NULL))
    """)
    suspend fun getDailyStatistics(date: LocalDate, category: String?): DailyStatistics?

    // 在这里，我犯了两个错误，一个是返回类型应该是 List，另一个是导错了包，应该是协程包，而不是 current.flow
    // 复制粘贴多了，自己写的少了，就容易出各种各样的错误。
    @Query("SELECT * FROM daily_statistics WHERE date = :date ORDER BY totalDuration DESC")
    fun getStatisticsFlowByDate(date: LocalDate): Flow<List<DailyStatistics>>

    @Insert
    suspend fun insert(dailyStatistics: DailyStatistics)

    @Update
    suspend fun update(dailyStatistics: DailyStatistics)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dailyStatistics: List<DailyStatistics>)

    @Update
    suspend fun updateAll(dailyStatistics: List<DailyStatistics>)

    @Query("SELECT * FROM daily_statistics WHERE date = :date ORDER BY totalDuration DESC")
    suspend fun getDailyStatisticsByDate(date: LocalDate): List<DailyStatistics>

    @Query("SELECT SUM(totalDuration) FROM daily_statistics WHERE date = :date")
    suspend fun getTotalDurationByDate(date: LocalDate): Duration?

    @Query("DELETE FROM daily_statistics WHERE date = :date AND category = :category")
    suspend fun deleteDailyStatistics(date: LocalDate, category: String)

    @Query("DELETE FROM daily_statistics")
    suspend fun deleteAll()

    @Query("""
        UPDATE daily_statistics 
        SET totalDuration = totalDuration + :deltaDuration
        WHERE date = :eventDate AND category = :category
    """)
    suspend fun updateCategoryDuration(eventDate: LocalDate, category: String, deltaDuration: Duration)

    @Query("""
        UPDATE daily_statistics 
        SET totalDuration = totalDuration - :duration 
        WHERE date = :date 
        AND ((category = :category AND :category IS NOT NULL) OR (category IS NULL AND :category IS NULL))
    """)
    suspend fun reduceDuration(date: LocalDate, category: String?, duration: Duration)

    @Query("""
        UPDATE daily_statistics 
        SET totalDuration = totalDuration + :duration 
        WHERE date = :date AND category = :category
    """)
    suspend fun increaseDuration(date: LocalDate, category: String, duration: Duration)

    @Query("DELETE FROM daily_statistics WHERE totalDuration = :duration")
    suspend fun deleteEntryByEmptyDuration(duration: Duration)

    @Query("DELETE FROM daily_statistics WHERE date = :date")
    suspend fun deleteAllByDate(date: LocalDate)

    @Query("SELECT date, totalDuration as duration FROM daily_statistics WHERE category = :category")
    suspend fun getDateDurationByCategory(category: String): DateDuration

    @Query("SELECT * FROM daily_statistics")
    suspend fun getAllDailyStatistics(): List<DailyStatistics>



}
