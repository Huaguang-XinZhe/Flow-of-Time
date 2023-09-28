package com.huaguang.flowoftime

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.data.repositories.DailyStatisticsRepository
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.sources.SPHelper
import com.huaguang.flowoftime.ui.pages.time_record.EventControlViewModel
import com.huaguang.flowoftime.ui.pages.time_record.event_buttons.EventButtonsViewModel
import com.huaguang.flowoftime.ui.pages.time_record.time_regulator.TimeRegulatorViewModel
import com.huaguang.flowoftime.ui.state.ButtonsState
import com.huaguang.flowoftime.ui.state.IdState
import com.huaguang.flowoftime.ui.state.PauseState
import com.huaguang.flowoftime.ui.state.SharedState
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var sharedState: SharedState // Activity 和 Fragment 各自注入，也不会出问题！
    @Inject
    lateinit var spHelper: SPHelper
    @Inject
    lateinit var idState: IdState
    @Inject
    lateinit var buttonsState: ButtonsState
    @Inject
    lateinit var pauseState: PauseState
//    @Inject
//    lateinit var iconRepository: IconMappingRepository
    @Inject
    lateinit var undoStack: UndoStack
    @Inject
    lateinit var eventRepository: EventRepository
    @Inject
    lateinit var dailyRepository: DailyStatisticsRepository
    @Inject
    lateinit var floatingWindowManager: FloatingWindowManager // 不能设为 private，否则会报错！

    private val eventControlViewModel: EventControlViewModel by viewModels()
    private val buttonsViewModel: EventButtonsViewModel by viewModels()
    private val regulatorViewModel: TimeRegulatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        iconRepository.preloadData() // 从数据库预加载映射数据到内存中

        val appViewModels = AppViewModels(
            eventControlViewModel, buttonsViewModel, regulatorViewModel
        )
        
        setContent {
            MyApp(appViewModels)
        }

        // 观察 LiveData 的变化
        observeLiveData()

    }

    override fun onStop() {
        super.onStop()
        RDALogger.info("回调 onStop()")

        spHelper.saveState(idState, buttonsState, pauseState,
            sharedState.cursorType, undoStack.serialize())
    }

    private fun observeLiveData() {
        sharedState.toastMessage.observe(this) { toastMessage ->
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
        }

        sharedState.categoryUpdate.observe(this) { categoryUpdate ->
            RDALogger.info("观察到 categoryUpdate")
            lifecycleScope.launch {
                val (date, previousCategory, duration) =
                    eventRepository.getEventCategoryInfoById(categoryUpdate.eventId)

                // 如果以前的类属（必须通过数据库获取）和现在的类属相同，那也不用继续了
                if (previousCategory == categoryUpdate.newCategory) return@launch

                dailyRepository.categoryReplaced(
                    date = date,
                    originalCategory = previousCategory,
                    newCategory = categoryUpdate.newCategory,
                    duration = duration
                )

            }
        }

        eventControlViewModel.requestPermissionSignal.observe(this) {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        PermissionX.init(this)
            .permissions(Manifest.permission.SYSTEM_ALERT_WINDOW) // 这里必须用 android 的 Manifest，不能用程序的包
            .onExplainRequestReason { scope, deniedList ->
                val message = "PermissionX需要您同意以下权限才能正常使用"
                scope.showRequestReasonDialog(deniedList, message, "同意", "算了")
            }
            .request { allGranted, _, deniedList ->
                if (allGranted) {
                    floatingWindowManager.initFloatingButton()
//                    startFloatingWindowService()
                    sharedState.toastMessage.value = "申请已通过"
                } else {
                    sharedState.toastMessage.value = "您拒绝了如下权限：$deniedList"
                }

            }
    }

//    private fun startFloatingWindowService() {
//        val intent = Intent(this, FloatingWindowService::class.java)
//        startService(intent)
//    }
}

