package com.huaguang.flowoftime.data.sources

//class LocalDataSource(private val database: EventDatabase) : DataSource {
//    override suspend fun getEvents(): Result<List<Event>> {
//        return try {
//            Result.Success(database.eventDao().getAllEvents())
//        } catch (e: Exception) {
//            Result.Error(e)
//        }
//    }
//    // 其他 DataSource 接口方法的实现...
//}
