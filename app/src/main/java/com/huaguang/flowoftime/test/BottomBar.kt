package com.huaguang.flowoftime.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huaguang.flowoftime.Page
import com.huaguang.flowoftime.tabs


@Preview(showBackground = true)
@Composable
fun TestBottomBar() {
    val currentRoute = remember { mutableStateOf(Page.Record.route) }
    BottomBar(currentRoute = currentRoute, onNavigation = { route ->

    })
}

/**
 * 自定义的 BottomNavigationBar 还不如官方给的呢。
 */
@Composable
fun BottomBar(
    currentRoute: MutableState<String>,
    onNavigation: (String) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier.fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Gray.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = 10f
                )
            )
            .padding(vertical = 10.dp)
    ) {
        tabs.forEach { page ->
            BottomTab(page, currentRoute, onNavigation)
        }
    }
}

@Composable
fun BottomTab(
    page: Page,
    currentRoute: MutableState<String>,
    onNavigation: (String) -> Unit,
) {
    val selected = currentRoute.value == page.route // 用 composable 内的 clicked 状态只能实现单个 Tab 的选中，并不能实现和其他 Tab 的交互。
    val selectedColor = if (selected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.8f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = {
                currentRoute.value = page.route
                onNavigation(page.route)
            },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                painter = painterResource(id = page.iconRes),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = selectedColor
            )
        }

        Text(
            text = stringResource(id = page.labelRes),
            color = selectedColor,
            fontSize = 12.sp,
        )
    }
}