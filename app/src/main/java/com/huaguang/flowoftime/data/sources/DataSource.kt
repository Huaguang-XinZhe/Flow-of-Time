package com.huaguang.flowoftime.data.sources

import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.wait.Result

interface DataSource {
    suspend fun getEvents(): Result<List<Event>>
}
