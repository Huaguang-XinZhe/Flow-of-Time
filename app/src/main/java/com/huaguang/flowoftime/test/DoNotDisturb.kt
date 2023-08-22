package com.huaguang.flowoftime.test

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.data.sources.SPHelper


/**
 * <del>免打扰的开关，要申请相应的权限：</del>
 * <del><uses-permission android:name="android.permission.ACCESS_NOTIFICATION_POLICY" /></del>
 *
 * 免打扰权限不需要在 AndroidManifest.xml 文件中声明，只需在运行时检查即可。
 */

@Composable
fun DoNotDisturbSwitch() {
    val context = LocalContext.current
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val isDoNotDisturbEnabled = remember {
        mutableStateOf(notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE)
    }

    if (!notificationManager.isNotificationPolicyAccessGranted) {
        Button(onClick = {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            context.startActivity(intent)
        }) {
            Text("请在设置中授权访问免打扰设置")
        }
        return
    }

    Switch(
        checked = isDoNotDisturbEnabled.value,
        onCheckedChange = { enabled ->
            isDoNotDisturbEnabled.value = enabled

            if (enabled) {
                // 启用 DND 模式
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            } else {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
        }
    )
}

@Composable
@Preview(showBackground = true)
fun Test() {
    DoNotDisturbSwitch()
}

/*
封装在 ViewModel 中-----------------------------------------------------------------------------------
 */

class DoNotDisturbViewModel(private val context: Context) : ViewModel() {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // LiveData 来观察免打扰状态
    private val _isDoNotDisturbEnabled = MutableLiveData(
        notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE
    )
    val isDoNotDisturbEnabled: LiveData<Boolean> get() = _isDoNotDisturbEnabled

    // 开关免打扰模式
    fun toggleDoNotDisturb(enable: Boolean) {
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            // 如果没有权限，这里可以设置一个标志或者发出一个事件通知界面跳转到设置
            return
        }

        if (enable) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        } else {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
        _isDoNotDisturbEnabled.value = enable
    }

    // 检查是否有权限
    fun hasNotificationPolicyAccess(): Boolean {
        return notificationManager.isNotificationPolicyAccessGranted
    }
}

@Composable
fun DoNotDisturbSwitch(viewModel: DoNotDisturbViewModel) {
    val isDoNotDisturbEnabled by viewModel.isDoNotDisturbEnabled.observeAsState(initial = false)
    val context = LocalContext.current

    if (!viewModel.hasNotificationPolicyAccess()) {
        Button(onClick = {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            context.startActivity(intent)
        }) {
            Text("请在设置中授权访问免打扰设置")
        }
        return
    }

    Switch(
        checked = isDoNotDisturbEnabled,
        onCheckedChange = { enabled ->
            viewModel.toggleDoNotDisturb(enabled)
        }
    )
}

/*
封装在类中--------------------------------------------------------------------------------------------
 */

class DoNotDisturbManager(private val context: Context) {
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val isDoNotDisturbEnabled: Boolean
        get() = notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE

    fun toggleDoNotDisturb(enable: Boolean) {
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            return
        }

        if (enable) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        } else {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }

    fun hasNotificationPolicyAccess(): Boolean {
        return notificationManager.isNotificationPolicyAccessGranted
    }
}

@Composable
fun DoNotDisturbSwitch(manager: DoNotDisturbManager) {
    val isDoNotDisturbEnabled = remember { mutableStateOf(manager.isDoNotDisturbEnabled) }
    val context = LocalContext.current

    if (!manager.hasNotificationPolicyAccess()) {
        Button(onClick = {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            context.startActivity(intent)
        }) {
            Text("请在设置中授权访问免打扰设置")
        }
        return
    }

    Switch(
        checked = isDoNotDisturbEnabled.value,
        onCheckedChange = { enabled ->
            manager.toggleDoNotDisturb(enabled)
            isDoNotDisturbEnabled.value = enabled
        }
    )
}

/*
调整铃声，以达到免打扰开关的目的。封装在类中。---------------------------------------------------------------
 */
class RingerControl1(private val context: Context) {

    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // 保存原始铃声音量
    // 注意，这是类创建时保存下来的铃音值，在开启免打扰模式前仍有可能改动，所以，在开启前，一定要更新保存！
    private var originalRingerVolume: Int = audioManager.getStreamVolume(AudioManager.STREAM_RING)

    // 检查当前是否为免打扰模式（铃声音量为0）
    val isDoNotDisturbEnabled: Boolean
        get() = audioManager.getStreamVolume(AudioManager.STREAM_RING) == 0

    // 启用/禁用免打扰模式
    fun toggleDoNotDisturb(enable: Boolean) {
        if (enable) {
            originalRingerVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING)
            audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0)
        } else {
            audioManager.setStreamVolume(AudioManager.STREAM_RING, originalRingerVolume, 0)
        }
    }
}

@Composable
fun DoNotDisturbSwitch(ringerControl1: RingerControl1) {
    val isDoNotDisturbEnabled = remember { mutableStateOf(ringerControl1.isDoNotDisturbEnabled) }

    Switch(
        checked = isDoNotDisturbEnabled.value,
        onCheckedChange = { enabled ->
            ringerControl1.toggleDoNotDisturb(enabled)
            isDoNotDisturbEnabled.value = enabled
        }
    )
}

@Composable
@Preview(showBackground = true)
fun Test2() {
    val context = LocalContext.current
    val ringerControl1 = RingerControl1(context)

    DoNotDisturbSwitch(ringerControl1)
}


/**
 * 调整铃声，以达到免打扰开关的目的。
 * @param context 这个 context 必须是 Activity 或 Fragment 类型的上下文
 */
class RingerControl(private val context: Context) {

    /**
     * 音频管理器，用于获取和设置铃声音量
     */
    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var spHelper: SPHelper = SPHelper.getInstance(context)

    /**
     * 系统的铃声音量，每次访问都从系统获取
     */
    private val systemRingerVolume: Int
        get() =  audioManager.getStreamVolume(AudioManager.STREAM_RING)

    companion object {
        // TODO: 免打扰开启时的铃声的音量，允许配置
        private const val RING_VOLUME = 0 // kotlin 的常量必须在伴生对象中定义
    }

    /**
     * 开启自定义免打扰
     */
    fun enableDoNotDisturb() {
        checkAuth()
        RDALogger.info("开启自定义免打扰")
        // 把当前铃声存入 sp
        spHelper.saveRingVolume(systemRingerVolume)
        // 设置自定义的铃声音量（免打扰的低铃音）
        audioManager.setStreamVolume(AudioManager.STREAM_RING, RING_VOLUME, 0)
    }

    /**
     * 关闭自定义免打扰
     */
    fun disableDoNotDisturb() {
        RDALogger.info("关闭自定义免打扰")
        if (systemRingerVolume <= RING_VOLUME) { // 之前开启了免打扰
            audioManager.setStreamVolume(AudioManager.STREAM_RING, spHelper.getRingVolume(), 0)
        }
    }

    private fun checkAuth() {
        /**
         * 通知管理器，用于判断是否获取 ACCESS_NOTIFICATION_POLICY 权限，不获取该权限设置铃声音量会报错
         */
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (!notificationManager.isNotificationPolicyAccessGranted) { // 检查授权，没授权的话就打开系统授权页面
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            context.startActivity(intent)
        }
    }
}