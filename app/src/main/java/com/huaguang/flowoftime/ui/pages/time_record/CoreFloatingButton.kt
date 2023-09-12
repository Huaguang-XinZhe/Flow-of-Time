package com.huaguang.flowoftime.ui.pages.time_record

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
import com.huaguang.flowoftime.ui.widget.LongPressFloatingActionButton

@Composable
fun CoreFloatingButton(
    viewModel: EventInputViewModel,
    modifier: Modifier = Modifier
) {

    if (viewModel.coreButtonNotShow()) return

    val eventControl = LocalEventControl.current
    val buttonsStateControl = LocalButtonsStateControl.current

    LongPressFloatingActionButton(
        onClick = {
            viewModel.onCoreFloatingButtonClick(eventControl, buttonsStateControl)
        },
        onLongClick = { viewModel.onCoreFloatingButtonLongClick() },
        modifier = modifier.size(48.dp) // 新的按钮大小会覆盖先前默认的设置
    ) {
        Icon(
            painter = painterResource(id = R.drawable.current_core),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)

        )
    }
}
