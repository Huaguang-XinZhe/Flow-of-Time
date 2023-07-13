package com.huaguang.flowoftime.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.data.models.EventTimes
import com.huaguang.flowoftime.data.models.EventWithSubEvents
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface EventDao {

    // 插入数据————————————————————————————————————————————————————————————————————————
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<Event>)

    @Transaction
    suspend fun insertEventWithSubEvents(eventWithSubEvents: EventWithSubEvents) {
        insertEvent(eventWithSubEvents.event)
        insertAll(eventWithSubEvents.subEvents)
    }

    // 更新数据——————————————————————————————————————————————————————————————————————
    @Update
    suspend fun updateEvent(event: Event)

    // 删除数据——————————————————————————————————————————————————————————————————————
    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: Long)

    // 查询数据———————————————————————————————————————————————————————————————————————
    // 1. 查询返回关系型数据
    @Transaction
    @Query("SELECT * FROM events WHERE parentId IS NULL AND eventDate = :eventDate")
    fun getEventsWithSubEvents(eventDate: LocalDate = LocalDate.now()): Flow<List<EventWithSubEvents>>

    @Transaction
    @Query("SELECT * FROM events WHERE parentId IS NULL AND eventDate BETWEEN :startDate AND :endDate")
    fun getEventsWithSubEvents(startDate: LocalDate, endDate: LocalDate): Flow<List<EventWithSubEvents>>

    @Transaction
    @Query("SELECT * FROM events WHERE parentId IS NULL")
    suspend fun getEventsWithSubEventsImmediate(): List<EventWithSubEvents>

    // 2. 查询多列数据
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

    // 3. 查询上一个
    @Query("SELECT * FROM events WHERE parentId IS NULL ORDER BY id DESC LIMIT 1")
    suspend fun getLastMainEvent(): Event?

    @Query("SELECT * FROM events WHERE endTime IS NULL ORDER BY id DESC LIMIT 1")
    suspend fun getLastIncompleteEvent(): Event

    @Query("SELECT * FROM events ORDER BY id DESC LIMIT 1")
    suspend fun getLastEvent(): Event

    @Query("SELECT MAX(id) FROM events")
    suspend fun getLastEventId(): Long

    @Query("SELECT MAX(id) FROM events WHERE parentId IS NULL")
    suspend fun getLastMainEventId(): Long

    // 4. 其他查询
    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEvent(eventId: Long): Event

    @Query("SELECT COUNT(*) FROM events WHERE parentId = :parentId")
    suspend fun countSubEvents(parentId: Long): Int

    @Query("SELECT * FROM events WHERE parentId = :mainEventId")
    suspend fun getSubEventsForMainEvent(mainEventId: Long): List<Event>

    @Query("SELECT * FROM events WHERE eventDate = :eventDate")
    fun getEventsOnSpecificDate(eventDate: LocalDate = LocalDate.now()): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE eventDate BETWEEN :startDate AND :endDate")
    fun getEventsWithinDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Event>>



//    @Transaction
//    @Query("SELECT * FROM events")
//    fun getAllEvents(): PagingSource<Int, Event>

//    @Query("SELECT * FROM events WHERE name IN (:names) AND eventDate = :currentDate " +
//            "AND duration IS NOT NULL")
//    suspend fun getFilteredEvents(
//        names: List<String>,
//        currentDate: LocalDate = LocalDate.now()
//    ): List<Event>

//    @RawQuery
//    suspend fun getEventsWithCustomQuery(query: SimpleSQLiteQuery): List<Event>
//
//    // TODO: 没有处理 SQL 注入风险
//    suspend fun getFilteredEvents(
//        names: List<String>,
//        currentDate: LocalDate = LocalDate.now()
//    ): List<Event> {
//        val condition = names.joinToString(" OR ") { "name LIKE '%$it%'" }
//        val queryString = "SELECT * FROM events WHERE eventDate = '$currentDate' " +
//                "AND duration IS NOT NULL AND ($condition)"
//
//        return getEventsWithCustomQuery(SimpleSQLiteQuery(queryString))
//    }


}
