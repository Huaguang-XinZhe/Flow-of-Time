package com.huaguang.flowoftime.ui.state

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.huaguang.flowoftime.TimeStreamApplication
import com.huaguang.flowoftime.data.models.EventCategoryUpdate
import com.huaguang.flowoftime.data.sources.SPHelper
import javax.inject.Inject

class SharedState @Inject constructor(
    val application: Application,
    spHelper: SPHelper,
) {
    val toastMessage = MutableLiveData<String>()
    val categoryUpdate = MutableLiveData<EventCategoryUpdate>()
    val cursorType = spHelper.getCursorType() // 指示当前最近的正在进行的事项的类型，null 代表当前没有事项正在进行

    /**
     * 判断主题事件是否正在进行。
     */
    fun isSubjectTiming() = cursorType.value != null

    /**
     * 调用分类器的分类方法，这将初始化 classifier 变量，即 KeywordClassifier 实例。
     */
    fun classify(name: String): String? {
        return (application as TimeStreamApplication).classifier.classify(name)
    }

    fun classify2(name: String): String? {
        return (application as TimeStreamApplication).classifier2.classify(name)
    }

    fun classify3(name: String): String? {
        return (application as TimeStreamApplication).classifier3.classify(name)
    }


}