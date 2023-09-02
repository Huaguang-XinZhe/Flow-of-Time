package com.huaguang.flowoftime.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class CustomScope private constructor() : CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    companion object {
        @Volatile
        private var INSTANCE: CustomScope? = null

        fun getInstance(): CustomScope {
            // 在这里，单例对象在第一次调用getInstance()时才会被创建。
            // 双重检查锁定，优化性能，使线程安全
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CustomScope().also { INSTANCE = it }
            }
        }
    }

    fun cancel() {
        job.cancel()
    }
}

