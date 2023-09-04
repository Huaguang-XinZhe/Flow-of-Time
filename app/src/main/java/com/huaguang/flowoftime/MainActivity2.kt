package com.huaguang.flowoftime

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import com.huaguang.flowoftime.pages.time_record.TimeRecordFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity2 : FragmentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val fragmentContainer = FragmentContainerView(this).apply {
            id = View.generateViewId()
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        setContentView(fragmentContainer)

        // 检查是否已经添加了 Fragment，以避免在配置更改（如屏幕旋转）后重复添加
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(fragmentContainer.id, TimeRecordFragment())
                .commit()
        }

    }
}

