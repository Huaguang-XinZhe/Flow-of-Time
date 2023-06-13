package com.huaguang.flowoftime.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.data.models.EventTimes
import com.huaguang.flowoftime.data.models.EventWithSubEvents
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<Event>)

    @Update
    suspend fun updateEvent(event: Event)

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEvent(eventId: Long): Event

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: Long)

    @Query("SELECT MAX(id) FROM events")
    suspend fun getLastEventId(): Long

//    @Query("SELECT * FROM events WHERE name IN (:names) AND eventDate = :currentDate " +
//            "AND duration IS NOT NULL")
//    suspend fun getFilteredEvents(
//        names: List<String>,
//        currentDate: LocalDate = LocalDate.now()
//    ): List<Event>

    @RawQuery
    suspend fun getEventsWithCustomQuery(query: SimpleSQLiteQuery): List<Event>

    // TODO: 没有处理 SQL 注入风险 
    suspend fun getFilteredEvents(
        names: List<String>,
        currentDate: LocalDate = LocalDate.now()
    ): List<Event> {
        val condition = names.joinToString(" OR ") { "name LIKE '%$it%'" }
        val queryString = "SELECT * FROM events WHERE eventDate = '$currentDate' " +
                "AND duration IS NOT NULL AND ($condition)"

        return getEventsWithCustomQuery(SimpleSQLiteQuery(queryString))
    }

    @Query("SELECT COUNT(*) FROM events WHERE parentId = :parentId")
    suspend fun countSubEvents(parentId: Long): Int


    @Query("SELECT * FROM events WHERE parentId IS NULL ORDER BY id DESC LIMIT 1")
    suspend fun getLastMainEvent(): Event?

    @Query("SELECT * FROM events WHERE endTime IS NULL ORDER BY id DESC LIMIT 1")
    suspend fun getLastIncompleteEvent(): Event


    @Query("SELECT MAX(id) FROM events WHERE parentId IS NULL")
    suspend fun getLastMainEventId(): Long

    @Query("SELECT * FROM events ORDER BY id DESC LIMIT 1")
    suspend fun getLastEvent(): Event

    @Transaction
    @Query("SELECT * FROM events WHERE parentId IS NULL AND eventDate = :eventDate")
    fun getEventsWithSubEvents(eventDate: LocalDate = LocalDate.now()): Flow<List<EventWithSubEvents>>

    @Transaction
    @Query("SELECT * FROM events WHERE parentId IS NULL AND eventDate BETWEEN :startDate AND :endDate")
    fun getEventsWithSubEvents(startDate: LocalDate, endDate: LocalDate): Flow<List<EventWithSubEvents>>


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

    @Query("""
        SELECT startTime, endTime
        FROM events 
        WHERE parentId = :parentId 
          AND (startTime BETWEEN :startCursor AND :now OR endTime BETWEEN :startCursor AND :now)
    """)
    suspend fun getSubEventTimesWithinRange(
        parentId: Long,
        startCursor: LocalDateTime?,
        now: LocalDateTime = LocalDateTime.now()
    ): List<EventTimes>


//    @Transaction
//    @Query("SELECT * FROM events")
//    fun getAllEvents(): PagingSource<Int, Event>

}
