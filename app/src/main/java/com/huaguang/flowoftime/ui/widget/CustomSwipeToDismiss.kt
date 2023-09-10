package com.huaguang.flowoftime.ui.widget

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.ui.state.SharedState
import com.huaguang.flowoftime.ui.theme.LightRed6

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CustomSwipeToDismiss(
    event: Event? = null,
    sharedState: SharedState,
    dismissed: () -> Unit,
    dismissContent: @Composable (RowScope.() -> Unit)
) {
    val dismissState = rememberDismissState()
    val isItemClicked = remember { mutableStateOf(false) }
    val isInputShow by sharedState.isInputShow

    val direction =
        if (isItemClicked.value) {
            setOf(DismissDirection.StartToEnd)
        } else setOf()

    val borderModifier = if (isItemClicked.value) {
        Modifier.border(2.dp, Color.Red, RoundedCornerShape(12.dp))
    } else Modifier

    if (dismissState.isDismissed(DismissDirection.StartToEnd)) { dismissed() }

    SwipeToDismiss(
        state = dismissState,
        modifier = Modifier
            .padding(8.dp)
            .clickable { // TODO: 这个地方有待优化，可以利用 StateHolder 精简
                when {
                    isInputShow -> sharedState.toastMessage.value = "当前状态禁止删除，确认后继续操作"
                    event?.endTime == null -> {
                        sharedState.toastMessage.value = "计时项禁止删除！"
                    }
                    else -> {
                        isItemClicked.value = !isItemClicked.value

                        if (isItemClicked.value) {
                            sharedState.toastMessage.value = "解除限制，可右滑删除"
                        }
                    }
                }

            }
            .then(borderModifier),
        directions = direction,
        dismissThresholds = {
            FractionalThreshold(0.35f)
        },
        background = {
            SwipeBackground(dismissState = dismissState)
        },
        dismissContent = dismissContent
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeBackground(dismissState: DismissState) {
    val isDefault = dismissState.targetValue == DismissValue.Default
    val color by animateColorAsState(
        when (dismissState.targetValue) {
            DismissValue.Default -> LightRed6
            DismissValue.DismissedToEnd -> Color.Red
            else -> Color.Green
        }
    )
    val scale by animateFloatAsState(
        // DismissValue.Default 是滑块达到阈值之前的状态
        if (isDefault) 0.75f else 1f
    )

    Box(
        modifier = Modifier
            .fillMaxSize() // 背景部分不撑到父容器那么大，就只会是刚刚好包含 Icon 的大小
            .clip(RoundedCornerShape(12.dp)) //必须放在这里，如果放在 SwipeToDismiss，会把 Card 的阴影给覆盖了。
            .background(color)
            .padding(start = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = null,
            modifier = Modifier.scale(scale),
            tint = if (isDefault) Color.Black else Color.White
        )
    }
}
