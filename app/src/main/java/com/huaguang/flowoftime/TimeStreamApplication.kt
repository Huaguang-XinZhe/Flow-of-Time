package com.huaguang.flowoftime

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.data.sources.EventDatabase
import com.huaguang.flowoftime.data.sources.IconDatabase
import dagger.hilt.android.HiltAndroidApp
import java.io.FileOutputStream
import javax.inject.Inject

@HiltAndroidApp
class TimeStreamApplication @Inject constructor() : Application() {
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "my_service_channel"

        // Migration
        val MIGRATION_1_4 = object : Migration(1, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("""
//                    CREATE TABLE inspirations (
//                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
//                        text TEXT NOT NULL,
//                        date TEXT NOT NULL,  -- 使用 TEXT 类型存储 LocalDate
//                        category TEXT -- 允许 category 为 NULL
//                    )
//                """.trimIndent())
            }
        }
    }

    val eventDB: EventDatabase by lazy {
        Room.databaseBuilder(
            context = this,
            klass = EventDatabase::class.java,
            name = "event_database"
        )
            .addMigrations(MIGRATION_1_4)
            .build()
    }

    val iconDB: IconDatabase
        get() = _iconDB
    private lateinit var _iconDB: IconDatabase

    val classifier: KeywordClassifier by lazy {
        initializeClassifier()
    }

    val classifier2: KeywordClassifier by lazy {
        initializeClassifier2()
    }

    val classifier3: KeywordClassifier by lazy {
        initializeClassifier3()
    }

    override fun onCreate() {
        super.onCreate()

        initializeIconDB()
//        createNotificationChannel()

        RDALogger.start("打标签喽").enableLogging(true)

    }

    private fun createNotificationChannel() {
        val channelName = "My Service Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun initializeIconDB() {
        val dbFile = this.getDatabasePath("icon_mapping.db")

        // Step 1: Check if the database already exists
        if (!dbFile.exists()) {
            // Step 2: Copy database from assets if it doesn't exist
            copyDatabaseFromAssets()
        }

        // Step 3: Initialize Room database
        _iconDB = Room.databaseBuilder(
            context = this,
            klass = IconDatabase::class.java,
            name = "icon_mapping.db"
        ).build()
    }

    private fun copyDatabaseFromAssets() {
        val assetManager = this.assets
        assetManager.open("icon_mapping.db").use { inputStream ->
            val outputFilePath = this.getDatabasePath("icon_mapping.db").path

            RDALogger.info("databasePath: $outputFilePath")

            FileOutputStream(outputFilePath).use { outputStream ->
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
                outputStream.flush()
            }
        }
    }

    private fun initializeClassifier(): KeywordClassifier {
        val classifier = KeywordClassifier()

        val routine = listOf("煮吃", "买吃", "洗澡", "洗漱", "厕", "外吃", "热吃", "吃晚饭", "买菜", "晾衣", "拿快递", "理发")
        val family = listOf("老妈", "爷爷")
        val rest = listOf("睡", "躺歇", "眯躺", "远观")
        val exercise = listOf("慢跑", "俯卧撑")
        val communication = listOf("聊天", "通话", "鸿", "铮", "师父", "小洁", "小聚",)
        val frame = listOf("财务", "收集库", "时间统计", "清整", "规划", "省思", "统析", "仓库", "预算",
            "纸上统计", "思考", "草思")
        val frontEnd = listOf("HTML", "CSS", "JavaScript", "前端", "Vue", "React")
        val underlyingTech = listOf("计算机底层", "图解网络")
        val fallow = listOf("散步", "打字", "抽烟", "吹风", "休闲")
        val breach = listOf("躺刷", "贪刷", "朋友圈", "刷视频", "QQ空间", "Q朋抖",
            "刷手机", "躺思", "刷抖音", "动态", "贪观")
        val masturbation = listOf("xxx", "泄", "淫",)
        val entertainment = listOf("续观", "看电影")
        val explore = listOf("探索", "逛鱼皮星球", )
        val spring = listOf("spring", "SV", "Spring", "sv")

        classifier.apply {
            insert(routine, "常务")
            insert(family, "家人")
            insert(rest, "休息")
            insert(exercise, "锻炼")
            insert(communication, "交际")
            insert(frame, "框架")
            insert(frontEnd, "前端")
            insert(underlyingTech, "底层")
            insert(spring, "Spring")
            insert("时光流", "时光流")
            insert(explore, "探索")
            insert(fallow, "休闲")
            insert(breach, "违破")
            insert(masturbation, "xxx")
            insert(entertainment, "娱乐")
        }

        return classifier
    }

    private fun initializeClassifier2(): KeywordClassifier {
        val classifier = KeywordClassifier()

        val dev = listOf(
            "当前核心", "开发", "时光流", "伴随", "bug", "列表项", "日期", "内存", "隐藏",
            "插入", "间隙", "按钮", "事项", "事件", "配置", "个人", "数据库", "弹窗", "自定义",
            "Android", "类属", "模块", "饼图", "重构", "崩溃", "建个表", "板块", "网址", "输入框",
            "检测", "点击", "上传", "扩展", "切换", "驼峰", "滑动", "网址",
        )
        val resistanceToInertia = listOf(
            "抗性", "惯性", "泄", "触碰", "拨弄",
        )

        classifier.apply {
            insert(dev, "开发")
            insert("探索", "探索")
            insert(resistanceToInertia, "抗性")
        }

        return classifier
    }

    private fun initializeClassifier3(): KeywordClassifier {
        val classifier = KeywordClassifier()
        // 二级分类
        val myFrame = listOf("框架", "锻炼", "休闲", "抗性", "整理", "探索")
        val breach = listOf("违破", "xxx")
        val programming = listOf("MySQL", "时光流", "底层", "Spring", "前端", "网络")
        val nonDiscretionaryTime = listOf("常务", "休息", "家人", "应对")
        // 一级分类
//        val myFreeTime = listOf("框架", "违破", "编程")

        classifier.apply {
            insert(myFrame, "框架")
            insert(breach, "违破")
            insert(programming, "编程")
            insert(nonDiscretionaryTime, "不可支配")
//            insert(myFreeTime, "自由支配")
        }

        return classifier
    }

}

