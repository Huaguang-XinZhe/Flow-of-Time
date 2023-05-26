package com.huaguang.flowoftime

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup

@Composable
fun MyPopup(offset: IntOffset) {
    Popup(
        alignment = Alignment.CenterStart,
        offset = offset
    ) {
        Card {
            Text(
                text = "我是 Popup！",
                color = Color.White,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

@Composable
fun MyText() {
    Log.i("打标签喽", "重组执行！")
    // 记录弹出框的位置
    val position = remember { mutableStateOf(IntOffset.Zero) }
    val showPopup = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        TextButton(
            onClick = { showPopup.value = !showPopup.value },
            modifier = Modifier
                .align(Alignment.Center)
                .onGloballyPositioned { layoutCoordinates ->
                    // 这里我们要获取 TextButton 的顶部中心点
                    val topLeft = layoutCoordinates.boundsInRoot().topLeft
                    val halfWidth = layoutCoordinates.boundsInRoot().width / 2
                    position.value = IntOffset((topLeft.x + halfWidth).toInt(), topLeft.y.toInt())
                    Log.i("打标签喽", "position.value = ${position.value}")
            }
        ) {
            Text(text = "时间：11:55")
        }
    }

    if (showPopup.value) {
        MyPopup(offset = position.value)
    }
}




