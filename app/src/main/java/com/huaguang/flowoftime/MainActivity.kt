package com.huaguang.flowoftime

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import com.huaguang.flowoftime.ui.pages.time_record.TimeRecordFragment
import com.huaguang.flowoftime.ui.state.SharedState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var sharedState: SharedState // Activity 和 Fragment 各自注入，也不会出问题！

    lateinit var fragmentContainer: FragmentContainerView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        fragmentContainer = FragmentContainerView(this).apply {
            id = View.generateViewId()
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        setContentView(fragmentContainer)

        // 检查是否已经添加了 Fragment，以避免在配置更改（如屏幕旋转）后重复添加
        // 在配置更改的时候，savedInstanceState 里边存有之前的 Fragment 的实例，在重启后会自动恢复，所以需要检验，以免反复添加。
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(fragmentContainer.id, TimeRecordFragment())
                .commit()
        }

        sharedState.toastMessage.observe(this) { toastMessage ->
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
        }

    }
}

