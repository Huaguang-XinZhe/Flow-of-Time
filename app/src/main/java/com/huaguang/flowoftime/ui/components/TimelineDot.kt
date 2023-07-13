package com.huaguang.flowoftime.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.R

@Composable
fun Dot(
    modifier: Modifier = Modifier,
    size: Dp = 8.dp,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color, shape = CircleShape)
    )
}

@Composable
fun PulsingDot(
    modifier: Modifier = Modifier,
    size: Dp = 10.dp,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .size(size)
            .background(color.copy(alpha = 0.3f), shape = CircleShape)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(size * pulseSize)
                .background(color, shape = CircleShape)
                .border(0.5.dp, Color.White, CircleShape)
        )
    }
}

@Composable
fun RotatingIcon(
    modifier: Modifier = Modifier,
    size: Dp = 15.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    var rotation by remember { mutableStateOf(0f) }
    val animateRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(durationMillis = 150, easing = LinearEasing)
    )

    Box(
        modifier = modifier
            .size(size)
            .background(color.copy(alpha = 0.3f), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = { rotation = if (rotation == 0f) 90f else 0f}, // TODO: 这一块肯定要暴露出来
            modifier = Modifier.rotate(animateRotation)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.enter),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun Test2() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        RotatingIcon()
    }
}