package com.huaguang.flowoftime

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.view.ContextThemeWrapper
import com.ardakaplan.rdalogger.RDALogger
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.huaguang.flowoftime.utils.KeyboardUtils
import com.huaguang.flowoftime.utils.KeyboardUtils.hideSoftKeyboard
import com.huaguang.flowoftime.utils.KeyboardUtils.showSoftKeyboard
import kotlin.math.absoluteValue

class FloatingWindowManager(private val context: Context) {

    private val themedContext = ContextThemeWrapper(context, R.style.FloatingButtonTheme)
    private lateinit var windowManager: WindowManager

    @SuppressLint("InflateParams")
    fun initFloatingButton() {
        KeyboardUtils.init(context) // 初始化软键盘弹收工具类
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val fabView = LayoutInflater.from(themedContext)
            .inflate(R.layout.floating_action_button, null, false)
        val fab = fabView.findViewById<FloatingActionButton>(R.id.floatingActionButton)

        val params = configureFabViewParams()

        windowManager.addView(fabView, params)

        setFabTouchListener(fab, params)
        setFabClickListener(fab, fabView)
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
            y = dpInt(400)
        }
    }

    private fun dpInt(value: Int) = (value * context.resources.displayMetrics.density).toInt()

    @SuppressLint("ClickableViewAccessibility")
    private fun setFabTouchListener(
        fab: FloatingActionButton,
        params: WindowManager.LayoutParams,
    ) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        fab.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    fab.alpha = 1f
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(fab, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    fab.alpha = 0.1f
                    if (deltaX.absoluteValue < 5 && deltaY.absoluteValue < 5) {
                        v.performClick()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun setFabClickListener(
        view: FloatingActionButton,
        fabView: View
    ) {
        view.setOnClickListener {
            val inputView = createInputView()
            val input = inputView.findViewById<EditText>(R.id.input)
            val confirmButton = inputView.findViewById<Button>(R.id.confirmButton)

            setInputTouchListener(inputView, input)
            setInputTextWatcher(input, confirmButton)
            setConfirmButtonClickListener(confirmButton, input, fabView)

            windowManager.addView(inputView, configureInputViewParams())
            showKeyboardAndHideFab(input, fabView)
        }
    }

    @SuppressLint("InflateParams")
    private fun createInputView(): View {
        return LayoutInflater.from(themedContext)
            .inflate(R.layout.layout_input, null, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setInputTouchListener(inputView: View, input: EditText) {
        inputView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE) {
                hideSoftKeyboard(input)
                true
            } else {
                false
            }
        }
    }

    private fun setInputTextWatcher(input: EditText, confirmButton: Button) {
        input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                confirmButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setConfirmButtonClickListener(confirmButton: Button, input: EditText, fabView: View) {
        confirmButton.setOnClickListener {
            val inputValue = input.text.toString()
            RDALogger.info("输入的值是: $inputValue")
            windowManager.removeView((confirmButton.parent as View))

            fabView.visibility = View.VISIBLE
        }
    }

    private fun showKeyboardAndHideFab(input: EditText, fabView: View) {
        input.postDelayed({
            showSoftKeyboard(input)
            fabView.visibility = View.INVISIBLE
        }, 100)
    }

}