package com.huaguang.flowoftime.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.ui.widget.LongPressFloatingActionButton

@Composable
fun OverlayButton() {
    LongPressFloatingActionButton(
        onClick = { /*TODO*/ },
        onLongClick = { /*TODO*/ }
    ) {
        Icon(
            painter = painterResource(id = R.drawable.write),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
    }
}