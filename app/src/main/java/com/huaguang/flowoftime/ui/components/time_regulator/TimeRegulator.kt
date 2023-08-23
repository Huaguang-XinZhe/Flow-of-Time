package com.huaguang.flowoftime.ui.components.time_regulator

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.R
import java.time.LocalDateTime

//@Preview(showBackground = true)
@Composable
fun TimeRegulator(initialTime: LocalDateTime, viewModel: TimeRegulatorViewModel) {

    val toggleState = remember { mutableStateOf(true) }
    var time by remember { mutableStateOf(initialTime) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = {
            // LocalDateTime 减 5 分钟不会改变 time 自身的引用，必须重新赋值才能引起 time 的变化
            time = time.minusMinutes(5)
        }) {
            Text("-5m")
        }
        
        IconButton(onClick = { time = time.minusMinutes(1) }) {
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

        IconButton(onClick = { time = time.plusMinutes(1) }) {
            Icon(
                painter = painterResource(id = R.drawable.add),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        TextButton(onClick = { time = time.plusMinutes(5) }) {
            Text("+5m")
        }

//        IconButton(onClick = { /*TODO*/ }) {
//            Icon(
//                painter = painterResource(id = R.drawable.switch_up_and_down),
//                contentDescription = null,
//                modifier = Modifier.size(24.dp),
//            )
//        }
//        Text(text = formatLocalDateTime(time))
        
    }
}

@Preview(showBackground = true)
@Composable
fun test() {
//    val context = LocalContext.current
//    val viewModel = TimeRegulatorViewModel(LocalDateTime.now(), SPHelper.getInstance(context))
//
//    TimeRegulator(viewModel = viewModel)
}
