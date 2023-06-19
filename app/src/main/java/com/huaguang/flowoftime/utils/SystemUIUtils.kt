package com.huaguang.flowoftime.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowManager

class SystemUIUtils(private val context: Context) {

    // 状态栏高度+ 应用可用高度（显示高度） + 输入法高度（0）+ 导航栏高度（0）；
    // 让输入框获取焦点和软键盘弹出画上等号；
    // 默认状态栏的高度不为 0，始终显示；

    private var statusBarHeightId: Int? = null
    private var navigationBarHeightId: Int? = null

    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    fun getStatusBarHeight(): Int {
        val resources: Resources = context.resources
        statusBarHeightId = statusBarHeightId ?:
        resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (statusBarHeightId!! > 0) {
            resources.getDimensionPixelSize(statusBarHeightId!!)
        } else 0
    }

    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    fun getNavigationBarHeight(): Int {
        val resources: Resources = context.resources
        navigationBarHeightId = navigationBarHeightId ?:
        resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (navigationBarHeightId!! > 0) {
            resources.getDimensionPixelSize(navigationBarHeightId!!)
        } else 0
    }

    fun getCoreHeights(): CoreHeights {
        val realHeight: Int
        val statusBarHeight: Int
        val navigationBarHeight: Int

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val windowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars()
//                        or WindowInsets.Type.ime()
            )

            realHeight = windowMetrics.bounds.height()
            statusBarHeight = insets.top
            navigationBarHeight = insets.bottom

        } else {
            val dm = DisplayMetrics()
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getRealMetrics(dm)

            realHeight = dm.heightPixels
            statusBarHeight = getStatusBarHeight()
            navigationBarHeight = getNavigationBarHeight()
        }

        val displayHeight = context.resources.displayMetrics.heightPixels

        return CoreHeights(
            realHeight = realHeight,
            statusBarHeight = statusBarHeight,
            displayHeight = displayHeight,
            navigationBarHeight = navigationBarHeight
        )
    }

    // 这方法没用，测出来的高度始终为 0，也说明了一个问题，displayHeight 本身就已经包括了软键盘的高度！！！

//    fun getIMEHeight(): Int {
//        val (realHeight, statusBarHeight, displayHeight, navigationBarHeight) = getCoreHeights()
//        // 高度都是准确的
//        val imeHeight = realHeight - (statusBarHeight + displayHeight + navigationBarHeight)
//
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            imeHeight
//        } else {
//            // 无输入法，无导航栏，导航栏仍有高度（不准确）
//            if (displayHeight == realHeight - statusBarHeight) {
//                // 这里少了导航栏的高度，因为不存在，但测出来的 navigationBarHeight 却有值，所以不能加上
//                realHeight - (statusBarHeight + displayHeight)
//            } else {
//                imeHeight
//            }
//        }
//    }

}


data class CoreHeights(
    val realHeight: Int,
    val statusBarHeight: Int,
    val displayHeight: Int,
    val navigationBarHeight: Int,
)