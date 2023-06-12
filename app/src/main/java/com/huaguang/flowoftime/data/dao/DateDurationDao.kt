package com.huaguang.flowoftime.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.huaguang.flowoftime.data.models.DateDuration
import java.time.LocalDate

@Dao
interface DateDurationDao {

    @Query("SELECT * FROM date_durations WHERE date = :date")
    suspend fun getDateDuration(date: LocalDate): DateDuration?

    @Query("SELECT * FROM date_durations")
    suspend fun getAll(): List<DateDuration>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDateDuration(dateDuration: DateDuration)

    @Update
    suspend fun updateDateDuration(dateDuration: DateDuration)

}
