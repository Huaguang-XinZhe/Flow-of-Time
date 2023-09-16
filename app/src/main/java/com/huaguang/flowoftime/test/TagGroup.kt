package com.huaguang.flowoftime.test

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TagGroup(
    tags: List<String>,
    onSelected: (String) -> Unit
) {
    var selectedTag by remember { mutableStateOf(tags.first()) } // 默认选中标签列表中的第一位

    LazyRow {
        items(tags) { tag ->
            Label(
                text = tag,
                selectedTag = selectedTag,
                onClick = {
                    selectedTag = it
                    onSelected(it) //选中之后执行的一些操作，交由调用者实现
                }
            )
        }
    }
}

@Composable
fun Label(
    text: String,
    selectedTag: String,
    onClick: (String) -> Unit
) {
    val isSelected = text == selectedTag

    val colors = if (isSelected) {
        ButtonDefaults.buttonColors()
    } else {
        ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.DarkGray
        )
    }

    val border = if (isSelected) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    } else ButtonDefaults.outlinedButtonBorder


    OutlinedButton(
        onClick = { onClick(text) },
        colors = colors,
        border = border,
        modifier = Modifier.padding(horizontal = 10.dp)
    ) {
        Text(text)
    }


}

@Preview(showBackground = true)
@Composable
fun LabelGroup() {
    val tags = listOf(
        "昨日", "全部"
    )

    TagGroup(
        tags = tags,
        onSelected = { tag ->
            println(tag)
        }
    )
}

//--------------------------------------------------------------------------------------------------

@Composable
fun ChipGroup(
    chips: List<String>,
    onChipSelected: (String) -> Unit
) {
    var selectedChip by remember { mutableStateOf(chips.first()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { chip ->
            Chip(
                text = chip,
                selected = chip == selectedChip,
                onClick = {
                    selectedChip = chip
                    onChipSelected(chip)
                }
            )
        }
    }
}

@Composable
fun Chip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        shadowElevation = if (selected) 4.dp else 1.dp,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MyScreen() {
    val chips = listOf("Apple", "Banana", "Cherry")
    ChipGroup(
        chips = chips,
        onChipSelected = { chip ->
            println("Selected chip: $chip")
        }
    )
}

//--------------------------------------------------------------------------------------------------


/**
 * @deprecated
 * 这是一个标签组，它将具备以下特性和功能：
 * 1. 横向；
 * 2. 多选一，且只能选一，有默认；
 *
 * 如此，这个组件就必须维护一个标签列表，而标签组件就得维护标签文本和选中态。
 *
 * 在一个标签组中只保持一个选中态，如果选中态改变，那么标签组要负责协调控制标签选中态的转移。
 *
 * @param tags 一个标签组中的所有标签文本的列表
 * @param states 通过索引，对应于标签文本的选中态列表
 */

@Composable
fun TagGroup(tags: List<String>, states: List<Boolean>) {
    val stateList = remember { mutableStateListOf(*states.toTypedArray()) }

    LaunchedEffect(stateList) {// 这副作用就是外插进来的，打破当前组件专一性的因素
        // 找到列表中选中的那个标签的索引（位置）
        val index = stateList.indexOf(true)
        // 拿着索引去标签列表中找，看到底是哪个标签（文本）被选中
        val text = tags[index]
        // TODO: 在这里根据不同的标签执行不同的逻辑

    }

    LazyRow {
        itemsIndexed(tags) { index, tag ->
            Label1(text = tag, index = index, stateList = stateList)
        }
    }
}

/**
 * @deprecated
 * 这是一个标签，它维护一个标签文本（本身）、一个以索引（外联）和一个可变的选中态列表（外联）。
 * 它们有点类似于这样的关系：标签文本 - 索引 - 选中态列表。用索引从选中态列表中取出来的值和标签文本相对应。
 *
 * 它内部使用 OutlinedButton 来实现，点击即实现颜色切换：点击 -> 选中 -> 深色。
 * @param stateList 必须要用 SnapshotStateList 类型，要不然列表内容的改变无法通知上级组件（观察者）重组。
 */
@Composable
fun Label1(
    text: String,
    index: Int,
    stateList: SnapshotStateList<Boolean>
) {
    val isSelected = stateList[index] // 获取列表中的选中态

    val colors = if (isSelected) {
        ButtonDefaults.buttonColors()
    } else {
        ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.DarkGray
        )
    }

    val border = if (isSelected) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    } else ButtonDefaults.outlinedButtonBorder


    OutlinedButton(
        onClick = {
            stateList.replaceAll { false }
            stateList[index] = true // 设置列表的选中态（双向交互）
        },
        colors = colors,
        border = border,
        modifier = Modifier.padding(horizontal = 10.dp)
    ) {
        Text(text)
    }
}


/**
 * @deprecated
 * 这是一个标签，它维护一个标签文本和一个可更改的选中态。
 * 由于选中态可更改，且需要能被上级指定，所以不能将选中态放入函参中，并直接用 Boolean 作为选中态的类型；
 * 也不能将选中态放入函数内部，作为可变的局部变量；而是需要将选中态放入函参的同时，用 MutableState 来包裹。
 *
 * 它内部使用 OutlinedButton 来实现，点击即实现颜色切换：点击 -> 选中 -> 深色。
 */
@Composable
fun Label0(text: String, isSelected: MutableState<Boolean>) {
    val colors = if (isSelected.value) {
        ButtonDefaults.buttonColors()
    } else {
        ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.LightGray
        )
    }

    OutlinedButton(
        onClick = {
            // 点击只能选中或不操作（已经选中）
            if (!isSelected.value) isSelected.value = true
        },
        colors = colors
    ) {
        Text(text)
    }
}

/**
 * @deprecated
 */
@Preview(showBackground = true)
@Composable
fun LabelGroup0() {
    val tags = listOf(
        "昨日", "全部"
    )
    val states = listOf(
        true, false
    )

    TagGroup(tags = tags, states = states)
}