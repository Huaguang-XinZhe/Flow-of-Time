package com.huaguang.flowoftime.ui.pages.display_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
import com.huaguang.flowoftime.ui.theme.DeepRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayPageTopBar(viewModel: EventInputViewModel) {
    val intervalDays by viewModel.latestXXXIntervalDaysFlow.collectAsState()

    TopAppBar(
        title = {
            Text(
                text = "展示页",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {  }
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = null,
                )
            }
        },
        actions = {
            NumberCircle(
                number = intervalDays,
                modifier = Modifier.padding(horizontal = 5.dp)
            ) {
                // TODO: 可查看本月性泄的点图
            }
        }
    )
}

@Composable
fun NumberCircle(
    modifier: Modifier = Modifier,
    number: Int,
    onClick: () -> Unit
) {
    if (number == 0) return // 如果是 0，就不需要显示了

    val bgColor = if (number <= 7) DeepRed else Color.DarkGray

    Box(
        modifier = modifier
            .padding(vertical = 5.dp) // 在这里加 padding 就会往外加
            .background(bgColor, shape = CircleShape)
            .clickable( // 把这一块放在往内加的 padding 前边，水波纹才会扩散开来，到圆圈外边去，否则就被向内的 padding 局限了，很难看到效果
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false)
            )
            .padding(horizontal = 4.dp) // 但在这里，就会往内加
            ,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun test() {
//    DisplayPageTopBar()
}