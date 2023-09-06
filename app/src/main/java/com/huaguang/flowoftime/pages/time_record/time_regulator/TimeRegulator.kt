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

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = { viewModel.adjustTimeAndHandleChange(-5, CustomTimeState, selectedTime) }) {
            Text("-5m")
        }
        
        IconButton(onClick = { viewModel.adjustTimeAndHandleChange(-1, CustomTimeState, selectedTime) }) {
            Icon(
                painter = painterResource(id = R.drawable.minus),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

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
                modifier = Modifier.size(24.dp)
            )
        }

        IconButton(onClick = { viewModel.adjustTimeAndHandleChange(1, CustomTimeState, selectedTime) }) {
            Icon(
                painter = painterResource(id = R.drawable.add),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        TextButton(onClick = { viewModel.adjustTimeAndHandleChange(5, CustomTimeState, selectedTime) }) {
            Text("+5m")
        }
        
    }
}
