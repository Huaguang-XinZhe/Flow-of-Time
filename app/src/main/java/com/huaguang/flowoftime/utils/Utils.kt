package com.huaguang.flowoftime.utils

import com.huaguang.flowoftime.coreEventKeyWords

fun isCoreEvent(name: String): Boolean {
    for (keyWord in coreEventKeyWords) {
        val contains = name.contains(keyWord, true)

        if (contains) return true
    }

    return false
}




