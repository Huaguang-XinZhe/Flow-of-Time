package com.huaguang.flowoftime.data.repositories

import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.data.dao.IconMappingDao
import com.huaguang.flowoftime.data.models.IconMapping
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class IconMappingRepository(private val dao: IconMappingDao) {

    private val dataCache: HashMap<String, String> = hashMapOf()

    fun getIconUrlByCategory(category: String?): String {
        val iconName = dataCache[category] ?: ""
        return IconMapping.getUrlByIconName(iconName)
    }

    fun preloadData() {
        val localScope = CoroutineScope(Dispatchers.IO + Job())

        localScope.launch {
            val dataFromDb = dao.getNotNullMapping() // 查询数据库中的数据
            val map = dataFromDb.associate { it.category!! to it.iconName }
            dataCache.putAll(map)  // 将数据加载到缓存中

            RDALogger.info("数据已加载到内存！")
            // 数据加载完成，取消这个 Job 和与它关联的所有协程
            localScope.cancel()
        }
    }

    fun observeMappingForClass(className: String, viewModelScope: CoroutineScope): Job {
        return dao.getMappingForClass(className).onEach { updatedMapping ->
            dataCache[updatedMapping.category!!] = updatedMapping.iconName
        }.launchIn(viewModelScope)  // 如果在 ViewModel 中使用，否则选择合适的 CoroutineScope
    }

}