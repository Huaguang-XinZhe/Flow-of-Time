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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.data.models.CustomTime
import com.huaguang.flowoftime.ui.pages.time_record.LocalSelectedTime
import com.huaguang.flowoftime.ui.pages.time_record.LocalToggleState

@Composable
fun TimeRegulator(
    customTimeState: MutableState<CustomTime?>, // 这个值在选中 TimeLabel 的时候才会传递过来，否则为 null
    viewModel: TimeRegulatorViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTime = LocalSelectedTime.current
    viewModel.selectedTime = selectedTime

    fun onClick(value: Long) {
        viewModel.onClick(value, customTimeState)
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

        TimeAdjustIconButton(1, R.drawable.add)

        TimeAdjustButton(5, "+5m")
    }
}

@Composable
fun PauseButton(viewModel: TimeRegulatorViewModel) {
    val toggleState = LocalToggleState.current
    val iconRes = if (toggleState.value) R.drawable.continute else R.drawable.pause

    // 创建一个变量来跟踪组件是否已初始化
    val initialized = remember { mutableStateOf(false) }

    LaunchedEffect(toggleState.value) {
        // 如果组件已初始化，才执行副作用
        if (initialized.value) {
            RDALogger.info("PauseButton 副作用执行！")
            viewModel.calPauseInterval(toggleState.value)
        } else {
            // 标记组件为已初始化
            initialized.value = true
        }
    }

    FilledIconToggleButton(
        checked = toggleState.value,
        onCheckedChange = {
            toggleState.value = it
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


