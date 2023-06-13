package com.huaguang.flowoftime.data.sources

import com.huaguang.flowoftime.Result
import com.huaguang.flowoftime.data.models.Event

interface DataSource {
    suspend fun getEvents(): Result<List<Event>>
}
