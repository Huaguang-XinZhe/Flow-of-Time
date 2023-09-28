package com.huaguang.flowoftime.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

object KeyboardUtils {

    private lateinit var imm: InputMethodManager

    /**
     * 初始化 InputMethodManager
     * @param context 应用程序的上下文
     */
    fun init(context: Context) {
        imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    /**
     * 显示软键盘
     * @param view 当前获得焦点的视图
     */
    fun showSoftKeyboard(view: View) {
        view.requestFocus()
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    /**
     * 隐藏软键盘
     * @param view 当前获得焦点的视图
     */
    fun hideSoftKeyboard(view: View) {
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
