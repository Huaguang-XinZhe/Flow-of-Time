package com.huaguang.flowoftime.test

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.huaguang.flowoftime.DashType
import com.huaguang.flowoftime.R
import kotlinx.coroutines.launch

@Preview(showBackground = true)
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetInputDialog(type: DashType = DashType.TAG) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
        sheetContent = {
            SheetContent(type = type)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        sheetState.show()
                    }
                },
                modifier = Modifier.padding(top = 500.dp)
            ) {
                Text(text = "Show Bottom Sheet")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetContent(
    type: DashType
) {
    val titleText = when(type) {
        DashType.TAG -> "新增/修改标签"
        DashType.CATEGORY_ADD -> "新增类属"
        DashType.CATEGORY_CHANGE -> "修改类属"
        else -> "新增类属和标签"
    }
    val text = remember { mutableStateOf("") }

    ConstraintLayout(
        modifier = Modifier.fillMaxWidth().height(250.dp)
    ) {
        val (prefixRef, titleRef, tipRef, closeButtonRef, inputRef, confirmButtonRef) = createRefs()

        Text(
            text = titleText,
            fontSize = 22.sp,
            modifier = Modifier.constrainAs(titleRef) {
                top.linkTo(parent.top, 20.dp)
                // 居中显示
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )

        Text(
            text = if (type.isTag()) "#" else "@",
            color = Color.DarkGray.copy(alpha = 0.8f),
            fontSize = 22.sp,
            modifier = Modifier.constrainAs(prefixRef) {
                end.linkTo(titleRef.start, 10.dp)
                // 对其 titleRef，居中显示
                top.linkTo(titleRef.top)
                bottom.linkTo(titleRef.bottom)
            }
        )

        if (type.isTag()) {
            Text(
                text = "多个标签之间以 ，或 , 相隔",
                color = Color.LightGray,
                fontSize = 12.sp,
                modifier = Modifier.constrainAs(tipRef) {
                    top.linkTo(titleRef.bottom, 3.dp)
                    // 居中显示
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )
        }

        IconButton(
            onClick = { /*TODO*/ },
            modifier = Modifier.constrainAs(closeButtonRef) {
                end.linkTo(parent.end)
                top.linkTo(parent.top, 2.dp)
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.close_fill),
                contentDescription = null,
                tint = Color.LightGray
            )
        }

        OutlinedTextField(
            value = text.value,
            onValueChange = { text.value = it },
            singleLine = true,
            modifier = Modifier.padding(horizontal = 10.dp).constrainAs(inputRef) {
                val ref = if (type.isTag()) tipRef else titleRef
                top.linkTo(ref.bottom, 25.dp)
                // 居中显示
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )

        Button(
            onClick = { /*TODO*/ },
            modifier = Modifier.constrainAs(confirmButtonRef) {
                bottom.linkTo(parent.bottom, 20.dp)
                // 居中显示
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.confirm),
                contentDescription = null,
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text("确认")
        }

    }
}

