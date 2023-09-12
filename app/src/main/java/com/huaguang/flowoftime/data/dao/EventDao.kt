package com.huaguang.flowoftime.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.data.models.db_returns.EventTimes
import com.huaguang.flowoftime.data.models.db_returns.InsertParent
import com.huaguang.flowoftime.data.models.tables.Event
import com.huaguang.flowoftime.other.EventWithSubEvents
import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface EventDao {

    @Query("SELECT * FROM events")
    suspend fun getAllEvents(): List<Event>

    @Query("""
        SELECT * FROM events 
        WHERE id >= (SELECT MAX(id) FROM events WHERE parentEventId IS NULL)
    """)
    fun getLatestRootEventAndChildren(): Flow<List<Event>>

    @Query("""
        WITH RECURSIVE LatestRoots AS (
            SELECT id FROM events WHERE parentEventId IS NULL ORDER BY id DESC LIMIT 2
        )
        SELECT * FROM events 
        WHERE id >= (SELECT MIN(id) FROM LatestRoots)
        AND id < (SELECT MAX(id) FROM LatestRoots)
    """)
    fun getSecondLatestRootEventAndChildren(): Flow<List<Event>>

    @Query("SELECT startTime FROM events WHERE id = :id")
    suspend fun getStartTimeById(id: Long): LocalDateTime

    @Query("SELECT duration FROM events WHERE parentEventId = :id AND type = :eventType")
    suspend fun getSubInsertDurationList(id: Long, eventType: EventType): List<Duration>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<Event>)

    @Query("UPDATE events SET startTime = :newTime, duration = :newDuration WHERE id = :id")
    suspend fun updateStartTimeAndDurationById(
        id: Long,
        newTime: LocalDateTime,
        newDuration: Duration?
    )

    @Query("UPDATE events SET endTime = :endTime, duration = :duration WHERE id = :id")
    suspend fun updateEndTimeAndDurationById(
        id: Long,
        endTime: LocalDateTime,
        duration: Duration
    )

    @Query("""
        UPDATE events 
        SET endTime = :endTime, duration = :duration, pauseInterval = :totalPauseInterval
        WHERE id = :id
    """)
    suspend fun updateThree(
        id: Long,
        duration: Duration,
        totalPauseInterval: Int,
        endTime: LocalDateTime
    )

    @Update
    suspend fun updateEvent(event: Event)

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: Long): Event

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

    @Query("SELECT type FROM events WHERE id = :id")
    suspend fun getEventTypeById(id: Long): EventType

    @Query("SELECT pauseInterval FROM events WHERE id = :id")
    suspend fun getPauseIntervalById(id: Long): Int

    @Query("SELECT duration FROM events WHERE id = :eventId")
    suspend fun getDurationById(eventId: Long): Duration

    @Query("SELECT endTime, duration FROM events WHERE id = :parentId")
    suspend fun getInsertParentById(parentId: Long): InsertParent

    @Query("UPDATE events SET duration = :newDuration WHERE id = :id")
    suspend fun updateDurationById(id: Long, newDuration: Duration)

    @Query("UPDATE events SET withContent = :value WHERE id = :id")
    suspend fun updateWithContentById(id: Long, value: Boolean)

    @Query("SELECT endTime FROM events WHERE parentEventId IS NULL ORDER BY id DESC LIMIT 1")
    suspend fun getLastSubjectEndTime(): LocalDateTime?


}
