package com.huaguang.flowoftime

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.text.Editable
import android.text.TextWatcher
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.text.toHtml
import androidx.lifecycle.MutableLiveData
import com.ardakaplan.rdalogger.RDALogger
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.huaguang.flowoftime.data.models.TouchData
import com.huaguang.flowoftime.utils.KeyboardUtils
import com.huaguang.flowoftime.utils.KeyboardUtils.hideSoftKeyboard
import com.huaguang.flowoftime.utils.KeyboardUtils.showSoftKeyboard
import com.huaguang.flowoftime.utils.vibrate
import kotlin.math.abs
import kotlin.math.absoluteValue

class FloatingWindowManager(
    private val context: Context,
) {

    val isFabClose = MutableLiveData(false)
    val inputStr = MutableLiveData(Data.EMPTY to "")
    var fab: FloatingActionButton? = null

    private val themedContext = ContextThemeWrapper(context, R.style.FloatingButtonTheme)
    private lateinit var windowManager: WindowManager
    private var isDraggable = false
    private lateinit var input: EditText

    @SuppressLint("InflateParams")
    fun initFloatingButton() {
        KeyboardUtils.init(context) // 初始化软键盘弹收工具类
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val fabView = LayoutInflater.from(themedContext)
            .inflate(R.layout.floating_action_button, null, false)
        fab = fabView?.findViewById(R.id.floatingActionButton) // fabView 和 fab 其实本质上是一样的

        val params = configureFabViewParams()

        windowManager.addView(fabView, params)

        setFabListener(params)

    }

    fun removeFloatingButton() {
        // fab 在 XML 中没有父布局，就不要用 parent 属性去获取了，可能会出现类型转换错误
        fab?.let { windowManager.removeView(it) }
    }

    fun handleSingleTap(text: String? = null) {
        RDALogger.info("按钮点击")
        if (isFabClose.value == true) {
            windowManager.removeView(input.parent as View)
            isFabClose.value = false // 恢复 fab 样式
            return
        }

        val inputView = createInputView()
        input = inputView.findViewById<EditText?>(R.id.input).apply {
            setText(text)
            setSelection(text?.length ?: 0)
        }
        val confirmButton = inputView.findViewById<Button>(R.id.confirmButton)

        setInputTouchListener(inputView)
        setInputTextWatcher(confirmButton) // 这个每次点击似乎都会设置，每次都会产生不同的实例
        setConfirmButtonClickListener(confirmButton)

        windowManager.addView(inputView, configureInputViewParams())

        input.postDelayed({
            showSoftKeyboard(input)
            // 改 FAB 为关闭样式
            isFabClose.value = true
        }, 100)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setFabListener(
        params: WindowManager.LayoutParams
    ) {
        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                handleSingleTap()
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                handleDoubleTap()
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                handleLongPress()
            }

            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                handleFling(e1, e2)
                return true
            }
        })

        val touchData = TouchData()

        fab?.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
            handleTouchEvent(v, event, params, touchData)
        }
    }

    private fun handleTouchEvent(
        v: View,
        event: MotionEvent,
        params: WindowManager.LayoutParams,
        touchData: TouchData,
    ): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
//                    RDALogger.info("按钮按下")
                touchData.apply {
                    x = params.x
                    y = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                }

                v.alpha = 1f // 按钮不透明
                vibrate(context) // 振动一次
                animateView(v, 0.8f)

                false // 返回 false 以便其他监听器（如长按）可以接收此事件
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isDraggable) return false
                params.x = touchData.x + (event.rawX - touchData.touchX).toInt()
                params.y = touchData.y + (event.rawY - touchData.touchY).toInt()
                windowManager.updateViewLayout(v, params)
                true
            }
            MotionEvent.ACTION_UP -> {
//                    RDALogger.info("按钮抬起")
                val deltaX = event.rawX - touchData.touchX
                val deltaY = event.rawY - touchData.touchY

                if (deltaX.absoluteValue < 5 && deltaY.absoluteValue < 5) {
                    v.performClick()
                }

                v.alpha = 0.1f
                animateView(v)
                isDraggable = false // 重置标志

                true
            }
            else -> false
        }
    }

    private fun handleDoubleTap() {
        RDALogger.info("按钮双击")
    }

    private fun handleLongPress() {
        RDALogger.info("按钮长按")
        isDraggable = true
    }

    private fun handleFling(e1: MotionEvent, e2: MotionEvent) {
        val diffX = e2.x - e1.x
        val diffY = e2.y - e1.y

        when {
            abs(diffX) > abs(diffY) && diffX > 0 -> RDALogger.info("向右滑动：diffX = $diffX")
            abs(diffX) > abs(diffY) && diffX < 0 -> {
                RDALogger.info("向左滑动：diffX = $diffX")
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                context.startActivity(intent)
            }
            abs(diffY) > abs(diffX) && diffY > 0 -> RDALogger.info("向下滑动：diffY = $diffY")
            abs(diffY) > abs(diffX) && diffY < 0 -> {
                RDALogger.info("向上滑动：diffY = $diffY")
                inputStr.value = Data.GET_LAST to ""
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun createInputView(): View {
        return LayoutInflater.from(themedContext)
            .inflate(R.layout.layout_input, null, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setInputTouchListener(inputView: View) {
        inputView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE) {
                hideSoftKeyboard(input)
            }
            false
        }
    }

    private fun setInputTextWatcher(confirmButton: Button) {
        input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                confirmButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setConfirmButtonClickListener(confirmButton: Button) {
        confirmButton.setOnClickListener {
            val htmlText = input.text.toHtml()
            inputStr.value = if (inputStr.value?.first == Data.GET_LAST) {
                Data.UPDATE to htmlText // 更新数据
            } else {
                Data.INSERT to htmlText // 插入数据
            }
            windowManager.removeView((confirmButton.parent as View))
            isFabClose.value = false // 恢复 FAB 的样式
        }
    }

    private fun configureInputViewParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, // 使用 0 的话，输入框获取焦点后，屏幕其他区域无法操作。
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.START
            x = 0
            y = 0
        }
    }

    private fun configureFabViewParams(): WindowManager.LayoutParams {
        val displayMetrics = context.resources.displayMetrics
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
            y = dpInt(350)
        }
    }

    private fun animateView(v: View, scaleXY: Float = 1f) {
        v.animate()
            .scaleX(scaleXY)
            .scaleY(scaleXY)
            .setDuration(200)
            .start()
    }

    private fun dpInt(value: Int) = (value * context.resources.displayMetrics.density).toInt()

}