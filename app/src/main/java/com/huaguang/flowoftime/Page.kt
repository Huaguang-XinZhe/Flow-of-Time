package com.huaguang.flowoftime

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

sealed class Page(
    val route: String,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
) {
    object Record : Page("record", R.string.record, R.drawable.record)
    object List : Page("list", R.string.list, R.drawable.list)
    object Statistic : Page("statistic", R.string.statistic, R.drawable.statistic)
    object Category : Page("category", R.string.category, R.drawable.category)
}

val items = listOf(
    Page.Record,
    Page.List,
    Page.Statistic,
    Page.Category,
)