package com.huaguang.flowoftime.ui.pages.time_record.time_regulator

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.ui.pages.time_record.LocalCustomTimeState
import com.huaguang.flowoftime.ui.pages.time_record.LocalSelectedTime

@Composable
fun TimeRegulator(
    // 这个值在选中 TimeLabel 的时候才会传递过来，否则为 null
    modifier: Modifier = Modifier,
    viewModel: TimeRegulatorViewModel = viewModel()
) {
    if (viewModel.inputState.show.value) return

    val selectedTime = LocalSelectedTime.current
    val customTimeState = LocalCustomTimeState.current
    viewModel.selectedTime = selectedTime
    viewModel.customTimeState = customTimeState

    @Composable
    fun TimeAdjustButton(value: Long, label: String) {
        TextButton(onClick = { viewModel.onClick(value) }) {
            Text(label)
        }
    }

    @Composable
    fun TimeAdjustIconButton(value: Long, iconRes: Int) {
        IconButton(onClick = { viewModel.onClick(value) }) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
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

        PauseButton(viewModel = viewModel)

        TimeAdjustIconButton(1, R.drawable.add_circle)

        TimeAdjustButton(5, "+5m")
    }
}

@Composable
fun PauseButton(viewModel: TimeRegulatorViewModel) {
    val checked by viewModel.checkedLiveData.observeAsState(initial = true)
    val checkedState = remember { mutableStateOf(checked) }
    val iconRes = if (checkedState.value) R.drawable.continute else R.drawable.pause

    LaunchedEffect(checked) { // 想要观察到 LiveData 的变化，一定要加上 observeAsState 才行！
//        RDALogger.info("副作用执行！checked 赋值。")
        checkedState.value = checked
    }

    FilledIconToggleButton(
        checked = checkedState.value,
        onCheckedChange = {
            viewModel.toggleChecked(it) // 通过 ViewModel 更新 LiveData 的值（也只能这样了）
        },
        modifier = Modifier.size(36.dp),
        enabled = viewModel.pauseButtonEnabled()
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}

