package com.huaguang.flowoftime

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class Page(
    val route: String,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
) : Parcelable {
    @Parcelize
    object Record : Page("record", R.string.record, R.drawable.record)

    @Parcelize
    object List : Page("list", R.string.list, R.drawable.list)

    @Parcelize
    object Statistic : Page("statistic", R.string.statistic, R.drawable.statistic)

    @Parcelize
    object Category : Page("category", R.string.category, R.drawable.category)

    @Parcelize
    object Inspiration: Page("inspiration", R.string.inspiration, R.drawable.inspiration)
}


val tabs = listOf(
    Page.Record,
    Page.List,
    Page.Statistic,
    Page.Category,
    Page.Inspiration,
)