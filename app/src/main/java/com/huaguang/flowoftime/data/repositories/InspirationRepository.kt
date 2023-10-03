package com.huaguang.flowoftime.data.repositories

import com.huaguang.flowoftime.data.dao.InspirationDao
import com.huaguang.flowoftime.data.models.tables.Inspiration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InspirationRepository(private val dao: InspirationDao) {

    fun getAllInspirationsFlow() = dao.getAllInspirationsFlow()

    suspend fun insert(inspiration: Inspiration) =
        withContext(Dispatchers.IO) {
            dao.insertInspiration(inspiration)
        }

    suspend fun deleteInspirationById(id: Long) =
        withContext(Dispatchers.IO) {
            dao.deleteInspirationById(id)
        }

    suspend fun getLastIdText() =
        withContext(Dispatchers.IO) {
            dao.getLastIdText()
        }



    suspend fun updateTextById(id: Long, newText: String) {
        withContext(Dispatchers.IO) {
            dao.updateTextById(id, newText)
        }
    }

    suspend fun insertAll(inspirations: List<Inspiration>) {
        withContext(Dispatchers.IO) {
            dao.insertAll(inspirations)
        }
    }
}