package com.huaguang.flowoftime

import androidx.lifecycle.AndroidViewModel
import com.huaguang.flowoftime.data.EventRepository
import com.huaguang.flowoftime.data.SPHelper

class EventsViewModel(
    private val repository: EventRepository,
    private val spHelper: SPHelper,
    application: TimeStreamApplication
) : AndroidViewModel(application) {



//    @SuppressLint("MutableCollectionMutableState")
//    val selectedEventIdsMap = mutableStateOf(mutableMapOf<Long, Boolean>())
//    val isEventNameNotClicked = derivedStateOf {
//        beModifiedEvent?.let { selectedEventIdsMap.value[it.id] == null } ?: true
//    }

//    val pager = Pager(
//        PagingConfig(pageSize = 25)
//    ) { eventDao.getAllEvents() }.flow





    private val dismissedItems = mutableSetOf<Long>() // 为了防止删除逻辑多次执行




    init {


//        // 目前主要是重置 coreDuration
//        resetStateIfNewDay()
    }





//    private fun toggleSelectedId(eventId: Long) {
//        val map = selectedEventIdsMap.value.toMutableMap() // 调用这个方法能创建一个新实例！！！
//        map[eventId] = !(map[eventId] ?: false)
//        selectedEventIdsMap.value = map
//    }











}
