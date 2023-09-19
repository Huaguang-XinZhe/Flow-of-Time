package com.huaguang.flowoftime.test

import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun APP() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationTest(navController = navController) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding) // 这个 innerPadding 会起到什么效果呢？
        ) {
//            val uri = "android-app://bloom.app"

//            composable(
//                route = "plantDetail?id={id}",
//                deepLinks = listOf(
//                    navDeepLink {
//                        uriPattern = "$uri/plant/{id}"
//                    }
//                )
//            ) { backStackEntry ->
//                // 回退栈条目获取的 arguments 是一个可空的 Bundle 对象
//                PlantDetail(navController = navController, id = backStackEntry.arguments?.getString("id"))
//            }

            composable(
                route = "plantDetail/{plantId}?fromBanner = {fromBanner}", // 我故意在可选参数处加了空格，测试是否路由是否严格
                arguments = listOf(
                    navArgument("fromBanner") {
                        type = NavType.BoolType
                        defaultValue = true // 建议设置，可降低导航的调用成本
                    }
                )
            ) { backStackEntry ->
                // 回退栈条目获取的 arguments 是一个可空的 Bundle 对象
//                PlantDetail(navController = navController, id = backStackEntry.arguments?.getString("id"))
            }

            composable(Screen.Home.route) {
                HomeScreen(navController)
            }
            composable(Screen.Favorite.route) {
                FavoriteScreen(navController)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(navController)
            }
            composable(Screen.Cart.route) {
                CartScreen(navController)
            }
        }
    }

}

@Composable
fun BottomNavigationTest(navController: NavController) {
    BottomNavigation {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { screen ->
            BottomNavigationItem(
                icon = { Icon(imageVector = screen.icon, contentDescription = null) },
                label = { Text(text = stringResource(id = screen.resourceId)) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true, // 这里的 hierarchy 还是不太明白
                onClick = {
                    navController.navigate(screen.route) {
                        // 弹出（清除）跳转前页面在回退栈之前的所有栈条目，不包括页面本身
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true // 避免多次点击产生多个栈条目
                        restoreState = true // 再次点击之前的 item，恢复状态
                    }
                }
            )
        }
    }
}

@Composable
fun PlantDetail(
    navController: NavHostController,
    id: String?,
) {
    Button(onClick = { navController.navigate(Screen.Cart.route) }) {
        Text(text = id ?: "null")
    }
}

@Composable
fun HomeScreen(navController: NavHostController) {
    Button(onClick = { navController.navigate(Screen.Cart.route) }) {
        Text(text = "HomeScreen")
    }
}

@Composable
fun FavoriteScreen(navController: NavHostController) {
    Button(onClick = {
//        val request = NavDeepLinkRequest.Builder
//            .fromUri("android-app://bloom.app/plant/1234".toUri())
//            .build()
//
//        navController.navigate(request) // 走的是 deepLinks，不会走 route

        // 下面两个走的是 route 导航
//        navController.navigate("plantDetail") // 不指定参数
        // 注意，这里必须不能使用 `plantDetail/1234`，也不能使用 `plantDetail/id=1234`，除 `{}` 部分，其余不得改动！
        navController.navigate("plantDetail?id=1234") // 为可选参数指定值
    }) {
        Text(text = "FavoriteScreen")
    }
}

@Composable
fun ProfileScreen(navController: NavHostController) {
    Button(onClick = { navController.navigate(Screen.Home.route) }) {
        Text(text = "ProfileScreen")
    }
}

@Composable
fun CartScreen(navController: NavHostController) {
    Button(onClick = { navController.navigate(Screen.Home.route) }) {
        Text(text = "CartScreen")
    }
}