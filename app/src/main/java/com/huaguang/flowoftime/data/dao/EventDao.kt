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
import com.huaguang.flowoftime.data.models.db_returns.DateCategory
import com.huaguang.flowoftime.data.models.db_returns.EventCategoryInfo
import com.huaguang.flowoftime.data.models.db_returns.EventTimes
import com.huaguang.flowoftime.data.models.db_returns.InsertParent
import com.huaguang.flowoftime.data.models.db_returns.StopRequire
import com.huaguang.flowoftime.data.models.tables.Event
import com.huaguang.flowoftime.other.EventWithSubEvents
import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface EventDao {

    @Query("SELECT startTime, eventDate, category FROM events WHERE id = :eventId")
    suspend fun getStopRequireById(eventId: Long): StopRequire

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
    suspend fun getStepInsertDurationList(id: Long, eventType: EventType): List<Duration>

    @Query("SELECT duration FROM events WHERE id > :id AND type IN (:eventTypes)") // 只能应用于当前项，应用于非当前项会出错
    suspend fun getItemInsertDurationList(id: Long, eventTypes: List<EventType>): List<Duration>

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
        SET endTime = :endTime, duration = :duration, pauseInterval = :pauseInterval
        WHERE id = :id
    """)
    suspend fun updateThree(
        id: Long,
        duration: Duration?,
        pauseInterval: Int,
        endTime: LocalDateTime?
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


    /**
     * 获取范围内的所有事件，包括非主题事件！所以不能用 parentEventId = NULL 限制。
     */
    @Query("SELECT * FROM events WHERE eventDate BETWEEN :startDate AND :endDate")
    fun getAllWithinRangeEvents(startDate: LocalDate, endDate: LocalDate): Flow<List<Event>>

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

    @Query("UPDATE events SET name = :newName, category = :newCategory WHERE id = :id")
    suspend fun updateNameAndCategoryById(
        id: Long,
        newName: String,
        newCategory: String?
    )

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

    @Query("SELECT endTime FROM events WHERE id = :id")
    suspend fun getEndTimeById(id: Long): LocalDateTime

    @Query("UPDATE events SET tags = :tags WHERE id = :id")
    suspend fun updateTags(id: Long, tags: MutableList<String>)

    @Query("UPDATE events SET category = :category WHERE id = :id")
    suspend fun updateCategory(id: Long, category: String)

    @Query("UPDATE events SET category = :category, tags = :tags WHERE id = :id")
    suspend fun updateClassName(id: Long, category: String, tags: MutableList<String>?)

    @Query("SELECT eventDate FROM events WHERE category = :category ORDER BY startTime DESC LIMIT 1")
    fun getLatestXXXDate(category: String = "xxx"): Flow<LocalDate?>

    @Query("SELECT * FROM events WHERE id BETWEEN :startId + 1 AND :endId") // 由于 BETWEEN AND 会包含 startId，所以必须加一
    fun getEventsByIdRange(startId: Long, endId: Long): List<Event>

    @Query("SELECT eventDate, category FROM events WHERE id = :id")
    suspend fun getDateAndCategoryById(id: Long): DateCategory

    @Query("SELECT eventDate, category, duration FROM events WHERE id = :id")
    suspend fun getEventCategoryInfoById(id: Long): EventCategoryInfo


}
