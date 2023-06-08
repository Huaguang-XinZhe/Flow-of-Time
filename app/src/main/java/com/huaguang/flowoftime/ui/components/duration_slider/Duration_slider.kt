package com.huaguang.flowoftime.ui.components.duration_slider

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.utils.formatDurationInText

@Composable
fun DurationSlider(viewModel: DurationSliderViewModel) {
    val rate by viewModel.rate
    val coreDuration by viewModel.coreDuration
    val isAlarmSet by viewModel.isAlarmSet

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isAlarmSet) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = null,
                modifier = Modifier.padding(start = 8.dp, end = 5.dp)
            )
        }

        Text(
            text = "当下核心事务："
        )

        Slider(
            value = rate,
            enabled = false,
            onValueChange = {  },
            modifier = Modifier.weight(1f)
        )

        Text(
            text = rate.let { "%.1f".format(it.times(100)) + "%" },
            modifier = Modifier.padding(start = 8.dp)
        )

        Text(
            text = formatDurationInText(coreDuration),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
        )
    }
}