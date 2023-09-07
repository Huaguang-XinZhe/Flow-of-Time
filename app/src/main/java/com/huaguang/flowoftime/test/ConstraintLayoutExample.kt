package com.huaguang.flowoftime.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.layoutId

@Composable
fun ConstraintLayoutExample() {
    ConstraintLayout(
        constraintSet = ConstraintSet {
            val fab = createRefFor("fab")
            constrain(fab) {
                end.linkTo(parent.end, margin = 16.dp)
                bottom.linkTo(parent.bottom, margin = 16.dp)
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
    ) {
        FloatingActionButton(
            onClick = {},
            modifier = Modifier.layoutId("fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Test1() {
    ConstraintLayoutExample()
}