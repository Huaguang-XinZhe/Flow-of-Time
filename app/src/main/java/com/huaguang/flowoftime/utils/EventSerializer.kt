package com.huaguang.flowoftime.utils

import com.huaguang.flowoftime.data.Event
import java.time.LocalDate

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

    /**
     * 导入源文本中的第一条必须得是主事件；
     * 这个方法不支持这样形式的子事项：`……子事项5分钟`
     */
    fun importAnalysis(text: String, date: LocalDate): List<Pair<Event, List<Event>>> {
        val pattern = "(……)?(\\d{1,2}:\\d{2})?(.*?)(\\d{1,2}:\\d{2})?"
        val lines = text.split("\n")
        val eventsWithSubEvents = mutableListOf<Pair<Event, List<Event>>>()
        var mainEvent: Event? = null
        val subEvents = mutableListOf<Event>()

        lines.forEach { line ->
            val trimmedLine = line.trim()
            val matchResult = Regex(pattern).find(trimmedLine)
            if (matchResult != null) {
                var (ellipsis, startTime, name, endTime) = matchResult.destructured

                if (startTime.isEmpty()) startTime = "00:00"
                if (endTime.isEmpty()) endTime = "00:00"

                val event = Event(
                    startTime = parseToLocalDateTime(startTime, date),
                    name = name,
                    endTime = parseToLocalDateTime(endTime, date),
                )

                if (ellipsis.isEmpty()) {
                    // If we have a main event and its subevents, add them to the list
                    if (mainEvent != null && subEvents.isNotEmpty()) {
                        eventsWithSubEvents.add(mainEvent!! to subEvents.toList())
                        subEvents.clear()
                    }

                    mainEvent = event.copy(parentId = null)
                } else {
                    val subEvent = event.copy(
                        parentId = mainEvent!!.id
                    )
                    subEvents.add(subEvent)
                }
            }
        }

        // Add the last main event and its subevents to the list
        if (mainEvent != null && subEvents.isNotEmpty()) {
            eventsWithSubEvents.add(mainEvent!! to subEvents)
        }

        return eventsWithSubEvents
    }

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
