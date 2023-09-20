package com.huaguang.flowoftime.ui.pages.time_record

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.huaguang.flowoftime.Page

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPageTopBar(
    modifier: Modifier = Modifier,
    onNavigation: (String) -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "时间记录",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        navigationIcon = {
            IconButton(
                onClick = { onNavigation(Page.List.route) }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "go_list",
                )
            }
        },
        modifier = modifier

    )
}