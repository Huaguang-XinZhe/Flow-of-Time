package com.huaguang.flowoftime.data.repositories

import com.huaguang.flowoftime.data.dao.DailyStatisticsDao
import com.huaguang.flowoftime.data.models.tables.DailyStatistics
import com.huaguang.flowoftime.data.models.tables.Event
import com.huaguang.flowoftime.utils.getAdjustedEventDate
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

    fun getYesterdaysDailyStatisticsFlow(): Flow<List<DailyStatistics>> {
        val yesterday = getAdjustedEventDate().minusDays(0)
        return dailyStatisticsDao.getYesterdaysStatisticsFlow(yesterday)
    }

    /**
     * 插入或更新统计表，放在进入统计页时执行。
     * @param events 是需要更新的事件列表（在存档 id 之后，正在进行事件 id 之前的所有事件）
     */
    suspend fun upsertDailyStatistics(events: List<Event>) {
//        RDALogger.info("events = $events")
        // 创建一个映射来存储每日的统计数据
        val dailyStatisticsMap = mutableMapOf<Pair<LocalDate, String>, Duration>()
        // 创建两个列表来存储需要插入和需要更新的DailyStatistics条目
        val toInsert = mutableListOf<DailyStatistics>()
        val toUpdate = mutableListOf<DailyStatistics>()

        // 遍历所有的事件
        for (event in events) {
            // 确保事件有结束时间和类属
            if (event.endTime != null && event.category != null) {
                // 获取事件的日期和类属
                val date = event.eventDate!!
                val category = event.category!!

                // 更新映射中的时长
                val key = Pair(date, category)
                dailyStatisticsMap[key] = dailyStatisticsMap.getOrDefault(key, Duration.ZERO) + event.duration
            }
        }

//        RDALogger.info("解构遍历前 dailyStatisticsMap = $dailyStatisticsMap")
        // 遍历映射并准备daily_statistics表的数据
        for ((key, duration) in dailyStatisticsMap) {
            val (date, category) = key

            // 获取或创建DailyStatistics条目
            val dailyStat = withContext(Dispatchers.IO) {
                dailyStatisticsDao.getDailyStatistics(date, category)
                    ?: DailyStatistics(date = date, category = category, totalDuration = Duration.ZERO)
            }

            // 更新总时长
            dailyStat.totalDuration += duration

            // 根据ID将DailyStatistics条目添加到适当的列表中
            if (dailyStat.id == 0L) {
                toInsert.add(dailyStat)
            } else {
//                RDALogger.info("添加到更新列表")
                toUpdate.add(dailyStat)
            }
        }

        // 从代码上看，可能是全部插入也可能是全部更新，业务逻辑上也是有可能的，尽管概率很小，所以这样写可以保证程序的健壮性！
        withContext(Dispatchers.IO) {
            // 一次性插入和更新所有DailyStatistics条目
            if (toInsert.isNotEmpty()) {
                dailyStatisticsDao.insertAll(toInsert)
            }
            if (toUpdate.isNotEmpty()) {
//                RDALogger.info("执行更新")
                dailyStatisticsDao.updateAll(toUpdate)
            }
        }

    }

    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            dailyStatisticsDao.deleteAll()
        }
    }

}