package com.huaguang.flowoftime.ui.state

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import com.huaguang.flowoftime.TimeStreamApplication
import com.huaguang.flowoftime.data.sources.SPHelper
import javax.inject.Inject

class SharedState @Inject constructor(
    val application: Application,
    val spHelper: SPHelper,
) {
    // TODO: 这两个状态已经没用了
    val newEventName = mutableStateOf("")
    val isInputShow = mutableStateOf(false)
    val scrollIndex = mutableStateOf(0)

    val toastMessage = MutableLiveData<String>()
    val coreInputShow = mutableStateOf(false)
    val cursorType = spHelper.getCursorType() // 指示当前最近的正在进行的事项的类型，null 代表当前没有事项正在进行

    /**
     * 调用分类器的分类方法，这将初始化 classifier 变量，即 KeywordClassifier 实例。
     */
    fun classify(name: String): String? {
        return (application as TimeStreamApplication).classifier.classify(name)
    }


}