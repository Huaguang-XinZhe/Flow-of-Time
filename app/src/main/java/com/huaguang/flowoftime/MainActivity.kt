package com.huaguang.flowoftime

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.huaguang.flowoftime.data.User
import com.huaguang.flowoftime.viewModel.MyAppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val myApplication = application as MyApplication
            val database = myApplication.database
            val viewModel = MyAppViewModel(database)

            MyApp(viewModel)
        }
    }
}

@Composable
fun MyApp(viewModel: MyAppViewModel) {
    Log.i("打标签喽", "MyApp 重组了")
    val userList = viewModel.userList.observeAsState().value
        ?: listOf(User(name = "默认名字", age = 0))

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn {
            items(userList) { user ->
                Text("${user.name} ${user.age}")
            }
        }
        
        Button(onClick = {
            viewModel.insertUser()
        }) {
            Text("插入新用户")
        }

        Button(onClick = {
            viewModel.getAllUser()
        }) {
            Text("查询所有用户")
        }
    }
}