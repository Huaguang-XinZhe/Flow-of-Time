package com.huaguang.flowoftime.test

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppBar() {
    TopAppBar(
        title = {
            Text(
                text = "My App",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            // 你可以在这里添加导航图标，例如汉堡菜单或返回箭头
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color.White
            )
        },
        actions = {
            // 在这里添加其他操作按钮，例如搜索、分享等
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = Color.White
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
    )
}

@Preview(showBackground = true)
@Composable
fun MyApp() {
    Surface {
        Column {
            MyAppBar()
            // 你的其他 UI 组件
        }
    }
}