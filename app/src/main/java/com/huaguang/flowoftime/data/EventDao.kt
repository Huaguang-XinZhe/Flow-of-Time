package com.huaguang.flowoftime.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface EventDao {
    @Insert
    suspend fun insertEvent(event: Event): Long

    @Update
    suspend fun updateEvent(event: Event)

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEvent(eventId: Long): Event

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: Long)

    @Query("SELECT * FROM events WHERE name IN (:names) AND eventDate = :currentDate " +
            "AND duration IS NOT NULL")
    suspend fun getFilteredEvents(names: List<String>, currentDate: LocalDate): List<Event>

    @Query("SELECT * FROM events ORDER BY id DESC LIMIT 1")
    suspend fun getLastEvent(): Event

    @Query("SELECT MAX(id) FROM events WHERE parentId IS NULL")
    suspend fun getLastMainEventId(): Long?

    @Transaction
    @Query("SELECT * FROM events WHERE parentId IS NULL")
    fun getEventsWithSubEvents(): Flow<List<EventWithSubEvents>>

    @Query("SELECT * FROM events WHERE parentId = :mainEventId")
    suspend fun getSubEventsForMainEvent(mainEventId: Long): List<Event>


}
