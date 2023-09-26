package com.huaguang.flowoftime.data.repositories

import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.data.dao.DailyStatisticsDao
import com.huaguang.flowoftime.data.models.tables.DailyStatistics
import com.huaguang.flowoftime.data.models.tables.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDate

class DailyStatisticsRepository(
    private val dailyStatisticsDao: DailyStatisticsDao
) {
    /**
     * 根据带符号的变化量，更新统计表中的类属时长数据（累加）
     */
    suspend fun updateCategoryDuration(eventDate: LocalDate, category: String, deltaDuration: Duration) {
        withContext(Dispatchers.IO) {
            dailyStatisticsDao.updateCategoryDuration(eventDate, category, deltaDuration)
        }
    }

    fun getDailyStatisticsFlowByDate(date: LocalDate): Flow<List<DailyStatistics>> {
        return dailyStatisticsDao.getStatisticsFlowByDate(date)
    }

    suspend fun getDailyStatisticsByDate(date: LocalDate) =
        withContext(Dispatchers.IO) {
            dailyStatisticsDao.getDailyStatisticsByDate(date)
        }

    suspend fun upsertDailyStatistics(
        date: LocalDate,
        category: String?,
        duration: Duration
    ) {
        withContext(Dispatchers.IO) {
            // 获取或创建DailyStatistics条目
            val dailyStat = dailyStatisticsDao.getDailyStatistics(date, category)
                ?: DailyStatistics(date = date, category = category, totalDuration = Duration.ZERO)

            // 更新总时长
            dailyStat.totalDuration += duration

            // 插入或更新DailyStatistics条目
            if (dailyStat.id == 0L) {
                dailyStatisticsDao.insert(dailyStat)
            } else {
                dailyStatisticsDao.update(dailyStat)
            }
        }
    }

    suspend fun initializeDailyStatistics(allEvents: List<Event>) {
        // 创建一个映射来存储每日的统计数据
        val dailyStatisticsMap = mutableMapOf<Pair<LocalDate, String?>, Duration>()
        // 创建两个列表来存储需要插入和需要更新的DailyStatistics条目
        val toInsert = mutableListOf<DailyStatistics>()

        // 遍历所有的事件
        for (event in allEvents) {
            // 确保事件有结束时间
            if (event.endTime != null && event.parentEventId == null) { // 已经结束的主题事件
                // 获取事件的日期和类属
                val date = event.eventDate!!
                val category = event.category

                // 更新映射中的时长
                val key = Pair(date, category)
                dailyStatisticsMap[key] = dailyStatisticsMap.getOrDefault(key, Duration.ZERO) + event.duration
            }
        }

        // 遍历映射并准备daily_statistics表的数据
        for ((key, duration) in dailyStatisticsMap) { // 遍历的量和上次的不同，已经减少了
            val (date, category) = key
            // 创建DailyStatistics条目
            val dailyStat = DailyStatistics(date = date, category = category, totalDuration = duration)

            toInsert.add(dailyStat)
        }

        withContext(Dispatchers.IO) {
            // 一次性插入所有DailyStatistics条目
            dailyStatisticsDao.insertAll(toInsert)
        }

    }

    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            dailyStatisticsDao.deleteAll()
        }
    }

    suspend fun originalReduction(
        date: LocalDate,
        originalCategory: String,
        duration: Duration
    ) {
        withContext(Dispatchers.IO) {
            RDALogger.info("原来的减少：category = $originalCategory, duration = $duration")
            dailyStatisticsDao.reduceDuration(date, originalCategory, duration)
        }
    }

    suspend fun deleteEntryByEmptyDuration() {
        withContext(Dispatchers.IO) {
            dailyStatisticsDao.deleteEntryByEmptyDuration(Duration.ZERO)
        }
    }

    suspend fun deleteAllByDate(date: LocalDate) {
        withContext(Dispatchers.IO) {
            dailyStatisticsDao.deleteAllByDate(date)
        }
    }

    suspend fun getDateDurationByCategory(category: String) =
        withContext(Dispatchers.IO) {
            dailyStatisticsDao.getDateDurationByCategory(category)
        }

    suspend fun getAllDailyStatistics() =
        withContext(Dispatchers.IO) {
            dailyStatisticsDao.getAllDailyStatistics()
        }


}