package com.huaguang.flowoftime.pages.time_record.time_regulator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.data.models.CustomTime
import com.huaguang.flowoftime.pages.time_record.LocalSelectedTime

@Composable
fun TimeRegulator(
    CustomTimeState: MutableState<CustomTime?>,
    viewModel: TimeRegulatorViewModel,
    modifier: Modifier = Modifier
) {
    val toggleState = remember { mutableStateOf(true) }
    val selectedTime = LocalSelectedTime.current
    val iconSize = Modifier.size(24.dp)

    fun onClick(value: Long) {
        viewModel.debouncedOnClick(value, CustomTimeState, selectedTime)
    }

    @Composable
    fun TimeAdjustButton(value: Long, label: String) {
        TextButton(onClick = { onClick(value) }) {
            Text(label)
        }
    }

    @Composable
    fun TimeAdjustIconButton(value: Long, iconRes: Int) {
        IconButton(onClick = { onClick(value) }) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = iconSize,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeAdjustButton(-5, "-5m")

        TimeAdjustIconButton(-1, R.drawable.minus)

        FilledIconToggleButton(
            checked = toggleState.value,
            onCheckedChange = {
                toggleState.value = it
                viewModel.calPauseInterval(it)
            },
            modifier = Modifier.size(36.dp)
        ) {
            val iconRes = if (toggleState.value) R.drawable.continute else R.drawable.pause
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = iconSize
            )
        }

        TimeAdjustIconButton(1, R.drawable.add)

        TimeAdjustButton(5, "+5m")
    }
}



