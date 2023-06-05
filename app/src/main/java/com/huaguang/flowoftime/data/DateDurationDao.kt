package com.huaguang.flowoftime.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.time.LocalDate

@Dao
interface DateDurationDao {

    @Query("SELECT * FROM date_durations WHERE date = :date")
    suspend fun getDateDuration(date: LocalDate): DateDuration?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDateDuration(dateDuration: DateDuration)

    @Update
    suspend fun updateDateDuration(dateDuration: DateDuration)

}
