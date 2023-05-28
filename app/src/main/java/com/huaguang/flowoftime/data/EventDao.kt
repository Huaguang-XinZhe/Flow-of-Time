package com.huaguang.flowoftime.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface EventDao {
    @Insert
    suspend fun insertEvent(event: Event): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<Event>)

    @Update
    suspend fun updateEvent(event: Event)

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEvent(eventId: Long): Event

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: Long)

    @Query("SELECT * FROM events WHERE name IN (:names) AND eventDate = :currentDate " +
            "AND duration IS NOT NULL")
    suspend fun getFilteredEvents(
        names: List<String>,
        currentDate: LocalDate = LocalDate.now()
    ): List<Event>

    @Query("SELECT * FROM events ORDER BY id DESC LIMIT 1")
    suspend fun getLastEvent(): Event

    @Query("SELECT * FROM events WHERE endTime IS NULL ORDER BY id DESC LIMIT 1")
    suspend fun getLastIncompleteEvent(): Event


    @Query("SELECT MAX(id) FROM events WHERE parentId IS NULL")
    suspend fun getLastMainEventId(): Long?

    @Transaction
    @Query("SELECT * FROM events WHERE parentId IS NULL AND eventDate = :eventDate")
    fun getEventsWithSubEvents(eventDate: LocalDate = LocalDate.now()): Flow<List<EventWithSubEvents>>

    @Transaction
    @Query("SELECT * FROM events WHERE parentId IS NULL")
    suspend fun getEventsWithSubEventsImmediate(): List<EventWithSubEvents>


    @Query("SELECT * FROM events WHERE parentId = :mainEventId")
    suspend fun getSubEventsForMainEvent(mainEventId: Long): List<Event>

    @Transaction
    suspend fun insertEventWithSubEvents(eventWithSubEvents: EventWithSubEvents) {
        insertEvent(eventWithSubEvents.event)
        insertAll(eventWithSubEvents.subEvents)
    }



}
