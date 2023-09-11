package com.huaguang.flowoftime.utils

import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.data.models.tables.Event
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
                    type = EventType.SUBJECT // TODO: 随便写的，为了通过编译 
                )

                if (ellipsis.isEmpty()) {
                    // If we have a main event and its subevents, add them to the list
                    if (mainEvent != null && subEvents.isNotEmpty()) {
                        eventsWithSubEvents.add(mainEvent!! to subEvents.toList())
                        subEvents.clear()
                    }

                    mainEvent = event.copy(parentEventId = null)
                } else {
                    val subEvent = event.copy(
                        parentEventId = mainEvent!!.id
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
        val isGetUp = event.name == "起床"
        val exportST = formatLocalDateTime(event.startTime)

        val exportET =
            if (isGetUp) exportST else {
                event.endTime?.let { formatLocalDateTime(it) }
            }

        val exportDuration = if (isGetUp) exportET else {
            event.duration?.let { formatDuration(it) }
        }

        return "$exportST " +
                "${event.name.removeSpacesAndNewlines()} " +
                "$exportET " +
                exportDuration
    }

    private fun formatSubEvent(subEvent: Event): String {
        return "\uD83D\uDC4C " +
                "……${subEvent.name} " +
                "\uD83D\uDC4C " +
                "${subEvent.duration?.let { formatDuration(it) }}"
    }

    private fun String.removeSpacesAndNewlines(): String {
        return this.replace(Regex("[\\s\\n]"), "")
    }


}
