package com.huaguang.flowoftime.data.models

import com.huaguang.flowoftime.DBOperationType

data class InputConfirm(
    val type: DBOperationType,
    val taggedText: String, // 有可能是纯文本，也有可能是 html 文本（把区分标签加载后边，以 ``` 相隔）
    val category: String?
) {
    companion object {
        fun initialValue() = InputConfirm(
            type = DBOperationType.INVALID,
            taggedText = "",
            category = null
        )
    }
}
