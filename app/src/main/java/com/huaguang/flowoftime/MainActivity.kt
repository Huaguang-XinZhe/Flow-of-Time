package com.huaguang.flowoftime

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.ardakaplan.rdalogger.RDALogger
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
import kotlin.math.absoluteValue

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

    @SuppressLint("InflateParams")
    private fun showFloatingButton() {
        val themedContext = ContextThemeWrapper(this, R.style.FloatingButtonTheme)

        val fabView = LayoutInflater.from(themedContext)
            .inflate(R.layout.floating_action_button, null, false)
        val fab = fabView.findViewById<FloatingActionButton>(R.id.floatingActionButton)

        val params = configureWindowManagerParams()

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(fabView, params)

        setFloatingButtonTouchListener(fab, params, windowManager)

        fab.setOnClickListener {
//            sharedState.toastMessage.value = "悬浮按钮点击"
            val appContext = applicationContext
            Toast.makeText(appContext, "悬浮按钮点击", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configureWindowManagerParams(): WindowManager.LayoutParams {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = screenWidth - dpInt(45)
            y = dpInt(400)
        }
    }

    private fun dpInt(value: Int) = (value * resources.displayMetrics.density).toInt()

    @SuppressLint("ClickableViewAccessibility")
    private fun setFloatingButtonTouchListener(
        view: FloatingActionButton,
        params: WindowManager.LayoutParams,
        windowManager: WindowManager
    ) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    view.alpha = 1f
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(view, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    view.alpha = 0.1f
                    if (deltaX.absoluteValue < 5 && deltaY.absoluteValue < 5) {
                        v.performClick()
                    }
                    true
                }
                else -> false
            }
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
                    showFloatingButton()
                    sharedState.toastMessage.value = "申请已通过"
                } else {
                    sharedState.toastMessage.value = "您拒绝了如下权限：$deniedList"
                }

            }
    }
}

