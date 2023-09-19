package com.huaguang.flowoftime

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.huaguang.flowoftime.ui.state.SharedState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var sharedState: SharedState // Activity 和 Fragment 各自注入，也不会出问题！

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            APP()
        }

        sharedState.toastMessage.observe(this) { toastMessage ->
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
        }

    }
}

