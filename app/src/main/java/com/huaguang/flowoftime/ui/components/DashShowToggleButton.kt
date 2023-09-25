package com.huaguang.flowoftime.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.R

@Composable
fun DashShowToggleButton(
    dashButtonShow: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    FilledIconToggleButton(
        checked = dashButtonShow.value,
        onCheckedChange = {
            dashButtonShow.value = it
        },
        modifier = modifier
            .padding(5.dp)
            .size(24.dp) // padding 必须放在 size 前边，否则会向内挤
    ) {
        Icon(
            painter = painterResource(id = R.drawable.eye),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
    }
}