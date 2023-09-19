package com.huaguang.flowoftime.test

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import com.huaguang.flowoftime.R

sealed class Screen(
    val route: String,
    @StringRes val resourceId: Int, // 加这个注解有什么用？
    val icon: ImageVector,
) {
    object Home : Screen("home", R.string.home, Icons.Filled.Home)
    object Favorite : Screen("favorite", R.string.favorite, Icons.Filled.Favorite)
    object Profile : Screen("profile", R.string.profile, Icons.Filled.Person)
    object Cart : Screen("screen", R.string.cart, Icons.Filled.ShoppingCart)
}


val items = listOf(
    Screen.Home,
    Screen.Favorite,
    Screen.Profile,
    Screen.Cart
)
