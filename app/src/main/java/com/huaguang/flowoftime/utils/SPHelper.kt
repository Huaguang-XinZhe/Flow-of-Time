package com.huaguang.flowoftime.utils

import android.content.SharedPreferences

class SPHelper(private val sharedPreferences: SharedPreferences) {

    fun saveScrollIndex(index: Int) {
        sharedPreferences.edit().putInt("scroll_index", index).apply()
    }

    fun getScrollIndex(): Int {
        return sharedPreferences.getInt("scroll_index", -1)
    }

    fun clearScrollIndex() {
        sharedPreferences.edit().remove("scroll_index").apply()
    }
}
