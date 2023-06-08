package com.huaguang.flowoftime.utils

import android.content.Context
import android.widget.Toast
import com.huaguang.flowoftime.coreEventKeyWords
import dagger.hilt.android.qualifiers.ApplicationContext

fun isCoreEvent(name: String): Boolean {
    for (keyWord in coreEventKeyWords) {
        val contains = name.contains(keyWord, true)

        if (contains) return true
    }

    return false
}

fun showToast(message: String, @ApplicationContext context: Context) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

