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
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<Event>)

    @Query("UPDATE events SET startTime = :newTime, duration = :newDuration WHERE startTime = :originalTime")
    suspend fun updateEventByStartTime(
        originalTime: LocalDateTime,
        newTime: LocalDateTime,
        newDuration: Duration?
    )

    @Query("UPDATE events SET endTime = :newTime, duration = :newDuration WHERE endTime = :originalTime")
    suspend fun updateEventByEndTime(
        originalTime: LocalDateTime,
        newTime: LocalDateTime,
        newDuration: Duration
    )

    @Update
    suspend fun updateEvent(event: Event)

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEvent(eventId: Long): Event

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: Long)

    @Query("DELETE FROM events WHERE eventDate < :today")
    suspend fun deleteEventsExceptToday(today: LocalDate)

    @Query("SELECT MAX(id) FROM events")
    suspend fun getMaxEventId(): Long?

    @Query("SELECT * FROM events WHERE id = (SELECT MAX(id) FROM events)")
    fun getCurrentEventFlow(): Flow<Event?>

    @Query("SELECT * FROM events WHERE id = (SELECT MAX(id) FROM events)")
    suspend fun getCurrentEvent(): Event?

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

    @Query("SELECT COUNT(*) FROM events WHERE parentEventId = :parentEventId")
    suspend fun countSubEvents(parentEventId: Long): Int


    @Query("SELECT * FROM events WHERE parentEventId IS NULL ORDER BY id DESC LIMIT 1")
    suspend fun getLastMainEvent(): Event?

    @Query("SELECT * FROM events WHERE endTime IS NULL ORDER BY id DESC LIMIT 1")
    suspend fun getLastIncompleteEvent(): Event


    @Query("SELECT MAX(id) FROM events WHERE parentEventId IS NULL")
    suspend fun getLatestMainEventId(): Long // 只有创建的时候才会调用，所以直接取 max 没问题。

    @Query("SELECT * FROM events ORDER BY id DESC LIMIT 1")
    suspend fun getLastEvent(): Event

    @Query("SELECT MAX(id) FROM events")
    suspend fun getMaxId(): Long? // 当数据库中没有任何数据时，应该返回 null，否则计数会报错


    @Transaction
    @Query("SELECT * FROM events WHERE parentEventId IS NULL AND eventDate = :eventDate")
    fun getEventsWithSubEvents(eventDate: LocalDate = LocalDate.now()): Flow<List<EventWithSubEvents>>

    @Transaction
    @Query("SELECT * FROM events WHERE parentEventId IS NULL AND eventDate BETWEEN :startDate AND :endDate")
    fun getEventsWithSubEvents(startDate: LocalDate, endDate: LocalDate): Flow<List<EventWithSubEvents>>


    @Transaction
    @Query("SELECT * FROM events WHERE parentEventId IS NULL")
    suspend fun getEventsWithSubEventsImmediate(): List<EventWithSubEvents>

    @Transaction
    @Query("SELECT * FROM events WHERE parentEventId = :parentEventId")
    suspend fun getEventsWithSubEvents(parentEventId: Long): EventWithSubEvents

    @Transaction
    @Query("SELECT * FROM events WHERE parentEventId IS NULL AND eventDate = :yesterday")
    suspend fun getYesterdayEventsWithSubEventsImmediate(yesterday: LocalDate): List<EventWithSubEvents>


    @Query("SELECT * FROM events WHERE parentEventId = :eventId")
    suspend fun getContentEventsForEvent(eventId: Long): List<Event>

    @Transaction
    suspend fun insertEventWithSubEvents(eventWithSubEvents: EventWithSubEvents) {
        insertEvent(eventWithSubEvents.event)
        insertAll(eventWithSubEvents.subEvents)
    }

    @Query("""
        SELECT startTime, endTime
        FROM events 
        WHERE parentEventId = :parentEventId 
          AND (startTime BETWEEN :startCursor AND :now OR endTime BETWEEN :startCursor AND :now)
    """)
    suspend fun getSubEventTimesWithinRange(
        parentEventId: Long,
        startCursor: LocalDateTime?,
        now: LocalDateTime = LocalDateTime.now()
    ): List<EventTimes>

    @Query("UPDATE events SET name = :newName WHERE id = :id")
    suspend fun updateEventName(id: Long, newName: String)


//    @Transaction
//    @Query("SELECT * FROM events")
//    fun getAllEvents(): PagingSource<Int, Event>

}
