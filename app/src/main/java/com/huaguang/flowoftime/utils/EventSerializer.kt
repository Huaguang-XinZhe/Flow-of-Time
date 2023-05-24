package com.huaguang.flowoftime.utils

import com.huaguang.flowoftime.data.Event

object EventSerializer {

    fun exportEvents(eventsWithSubEvents: List<Pair<Event, List<Event>>>): String {
        val sb = StringBuilder()

        eventsWithSubEvents.forEach { (event, subEvents) ->
            sb.append(formatEvent(event))
            subEvents.forEach { subEvent ->
                sb.append("\n").append(formatSubEvent(subEvent))
            }
            sb.append("\n")
        }

        return sb.toString()
    }

//    fun importEvents(data: String): List<Pair<Event, List<Event>>> {
//        // 解析字符串，创建事件列表
//        // 这部分需要你编写具体的解析逻辑
//    }

    private fun formatEvent(event: Event): String {
        return "${formatLocalDateTime(event.startTime)} " +
                "${event.name} " +
                "${formatLocalDateTime(event.endTime!!)} " +
                formatDuration(event.duration!!)
    }

    private fun formatSubEvent(subEvent: Event): String {
        return " ……${subEvent.name} ${formatDuration(subEvent.duration!!)}"
    }
}
