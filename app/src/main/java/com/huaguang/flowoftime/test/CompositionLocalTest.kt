package com.huaguang.flowoftime.test

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

data class User(
    val name: String,
    val age: Int
)

val LocalUser = compositionLocalOf<User> { error("No User provided") }

@Composable
fun UserProvider(user: User, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalUser provides user) {
        content()
    }
}

@Composable
fun UserProfile() {
    val user = LocalUser.current
    // 使用 user
}

val LocalOnClick = compositionLocalOf { {} }

@Composable
fun ButtonComponent() {
    val onClick = LocalOnClick.current
    Button(onClick = onClick) {
        Text("Click Me")
    }
}

@Composable
fun AppContent() {
    val onClick: () -> Unit = { println("Button clicked!") }

    CompositionLocalProvider(LocalOnClick provides onClick) {
        ButtonComponent()
    }
}
