package com.huaguang.flowoftime.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.data.User
import com.huaguang.flowoftime.data.UserDatabase
import kotlinx.coroutines.launch

class MyAppViewModel(private val database: UserDatabase): ViewModel() {
    private val userDao = database.userDao()

    // 将 userList 改为 MutableLiveData
    val userList: MutableLiveData<List<User>> = MutableLiveData()

    fun insertUser() {
        val user = User(name = "刘志辉", age = 21)

        viewModelScope.launch {
            userDao.insertUser(user)
        }
    }

    fun getAllUser() {
        viewModelScope.launch {
            // 更新 userList 的值
            userList.value = userDao.getAllUser()
        }
    }
}
