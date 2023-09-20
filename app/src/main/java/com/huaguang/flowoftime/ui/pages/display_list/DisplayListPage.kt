package com.huaguang.flowoftime.ui.pages.display_list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.data.models.CombinedEvent
import com.huaguang.flowoftime.ui.components.ClassNameInputAlertDialog
import com.huaguang.flowoftime.ui.components.event_input.EventInputField
import com.huaguang.flowoftime.ui.components.toggle_item.DRToggleItem
import com.huaguang.flowoftime.ui.state.ItemState
import com.huaguang.flowoftime.ui.theme.PurpleWhite
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DisplayListPage(
    viewModel: DRListViewModel = viewModel(),
) {
    RDALogger.info("展示页重新组合")

    val recentTwoDaysCombinedEvents by viewModel.recentTwoDaysCombinedEventsFlow.collectAsState()
    if (recentTwoDaysCombinedEvents.contains(null)) return // 列表中含有空值就返回，不显示 UI

    // 为避免设值时触发重组，不能使用 MutableStateMapOf，只能使用普通的可变 Map。
    val toggleMap = remember { mutableMapOf<Long, ItemState>() } // Map 必须放在外边，如果放在 items 块内，对于每个 item 就都会创建一个 map
    val groupedEvents = recentTwoDaysCombinedEvents.groupBy { combinedEvent ->
        combinedEvent?.event?.eventDate ?: LocalDate.now()
    } // 先分组，后遍历

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        DisplayPageTopBar()

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            groupedEvents.forEach { (date, events) ->
                stickyHeader {
                    DateItem(date = date)
                }

                items(events) { item: CombinedEvent? ->
                    val eventId = item!!.event.id
                    if (toggleMap[eventId] == null) { // items 块会多次执行（列表滑动引起），为保证状态实例的唯一性，必须进行 null 检查，只有为 null 时才重新创建
                        toggleMap[eventId] = ItemState.initialDisplay()
                    }

                    DRToggleItem(
                        modifier = Modifier.padding(bottom = 5.dp),
                        itemState = toggleMap[eventId] ?: ItemState.initialDisplay(),
                        combinedEvent = item,
                    )
                }
            }

            item {// 尾部 Item
                Text(
                    text = "~~ 到底了哦 ~~",
                    modifier = Modifier.padding(20.dp)
                )
            }
        }
    }

    EventInputField(
        modifier = Modifier.padding(top = 300.dp),
    )

    ClassNameInputAlertDialog()

}

@Composable
fun DateItem(date: LocalDate) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PurpleWhite),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = date.toString(),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.padding(bottom = 10.dp),
        )

        Spacer(
            modifier = Modifier
//                .padding(bottom = 5.dp) // 放在这里才能向外撑，放在下边不行
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color.LightGray)
        )
    }
}

