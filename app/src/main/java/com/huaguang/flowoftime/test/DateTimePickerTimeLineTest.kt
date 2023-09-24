package com.huaguang.flowoftime.test

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.foreverrafs.datepicker.DatePickerTimeline
import com.foreverrafs.datepicker.Orientation
import com.foreverrafs.datepicker.state.rememberDatePickerState
import java.time.LocalDate

@Preview(showBackground = true)
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DatePickerTest() {
    val today = LocalDate.now()
    val datePickerState = rememberDatePickerState(initialDate = today)

    DatePickerTimeline(
        modifier = Modifier.wrapContentSize(),
        onDateSelected = { selectedDate: LocalDate ->
            // do something with the selected date
        },
        backgroundColor = MaterialTheme.colorScheme.background,  // the main background color
        state = datePickerState,
        orientation = Orientation.Vertical,
        selectedBackgroundColor = MaterialTheme.colorScheme.primary, // The background of the currently selected date
        selectedTextColor = Color.White,  // Text color of currently selected date
        dateTextColor = Color.Black, //Text color of all dates
        eventDates = listOf(
            today.plusDays(1),
            today.plusDays(3),
            today.plusDays(5),
            today.plusDays(8),
        ),
        todayLabel = {
//            TextButton(onClick = { datePickerState.smoothScrollToDate(today) }) {
//                Text(
//                    modifier = Modifier.padding(10.dp),
//                    text = "Today",
//                    color = Color.Black,
//                    style = MaterialTheme.typography.titleLarge
//                )
//            }
        },
        pastDaysCount = 10,  // The number of previous dates to display, relative to the initial date. Defaults to 120
        eventIndicatorColor = Color.DarkGray // Indicator color for marked event dates.
    )
}