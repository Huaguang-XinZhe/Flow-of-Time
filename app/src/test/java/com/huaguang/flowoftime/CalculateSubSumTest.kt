package com.huaguang.flowoftime

import com.huaguang.flowoftime.data.models.db_returns.EventTimes
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import java.time.Duration
import java.time.LocalDateTime

class CalculateSubSumTest {
    private fun calculateSubSum(eventTimes: List<EventTimes>, start: LocalDateTime): Duration {
        // 提取出重复的代码为一个局部函数
        fun calculatePairDurations(pairs: List<LocalDateTime>): Duration {
            return pairs.zipWithNext()
                .sumOf { (first, second) ->
                    Duration.between(first, second).toMillis()
                }.let { Duration.ofMillis(it) }
        }

        // 将 EventTimes 列表转化为 LocalDateTime 列表，并进行排序
        val times = eventTimes
            .flatMap { listOfNotNull(it.startTime, it.endTime) }
            .sorted()

        return if (times.size % 2 == 0) {
            // 对于偶数个时间点，将它们两两配对，然后计算每一对的差值，最后将所有的差值相加
            calculatePairDurations(times)
        } else {
            // 对于奇数个时间点，先计算第一个时间点和 start 的差值，然后将剩余的时间点两两配对，计算每一对的差值，最后将所有的差值相加
            Duration.between(start, times[0]) + calculatePairDurations(times.drop(1))
        }
    }

    @Test
    fun testCalculateSubSum() {
        // 创建 EventTimes 列表
        val eventTimes = listOf(
            EventTimes(
                LocalDateTime.of(2022, 3, 1, 12, 0),
                LocalDateTime.of(2022, 3, 1, 13, 0)
            ),
            EventTimes(
                LocalDateTime.of(2022, 3, 2, 12, 0),
                LocalDateTime.of(2022, 3, 2, 13, 0)
            )
        )

        // 创建 start 时间
        val start = LocalDateTime.of(2022, 3, 1, 11, 0)

        // 调用 calculateSubSum 方法
        val result = calculateSubSum(eventTimes, start)

        // 预期的结果
        val expected = Duration.ofHours(2)

        // 使用 assertEquals 检查结果是否正确
        assertEquals(expected, result)
    }
}
