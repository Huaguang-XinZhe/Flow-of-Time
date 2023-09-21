package com.huaguang.flowoftime.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.huaguang.flowoftime.data.models.tables.DailyStatistics
import java.time.Duration
import java.time.LocalDate

@Dao
interface DailyStatisticsDao {

    @Query("SELECT * FROM daily_statistics WHERE date = :date AND category = :category")
    suspend fun getDailyStatistics(date: LocalDate, category: String): DailyStatistics?

    @Insert
    suspend fun insert(dailyStatistics: DailyStatistics)

    @Update
    suspend fun update(dailyStatistics: DailyStatistics)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dailyStatistics: List<DailyStatistics>)

    @Query("SELECT * FROM daily_statistics WHERE date = :date")
    suspend fun getDailyStatisticsByDate(date: LocalDate): List<DailyStatistics>

    @Query("SELECT SUM(totalDuration) FROM daily_statistics WHERE date = :date")
    suspend fun getTotalDurationByDate(date: LocalDate): Duration?

    @Query("DELETE FROM daily_statistics WHERE date = :date AND category = :category")
    suspend fun deleteDailyStatistics(date: LocalDate, category: String)
}
