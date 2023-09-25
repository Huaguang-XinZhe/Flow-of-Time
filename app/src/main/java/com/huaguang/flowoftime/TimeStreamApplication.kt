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
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create a new temporary table with the desired structure
                database.execSQL("""
            CREATE TABLE daily_statistics_temp (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                date TEXT NOT NULL,
                category TEXT,
                totalDuration INTEGER NOT NULL
            )
        """)

                // Copy the data from the old table to the new temporary table
                database.execSQL("""
            INSERT INTO daily_statistics_temp (id, date, category, totalDuration)
            SELECT id, date, category, totalDuration FROM daily_statistics
        """)

                // Remove the old table
                database.execSQL("DROP TABLE daily_statistics")

                // Rename the new temporary table to the original table name
                database.execSQL("ALTER TABLE daily_statistics_temp RENAME TO daily_statistics")
            }
        }
    }

    val eventDB: EventDatabase by lazy {
        Room.databaseBuilder(
            context = this,
            klass = EventDatabase::class.java,
            name = "event_database"
        )
            .addMigrations(MIGRATION_2_3)
            .build()
    }

    val iconDB: IconDatabase
        get() = _iconDB
    private lateinit var _iconDB: IconDatabase

    val classifier: KeywordClassifier by lazy {
        initializeClassifier()
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

        val routine = listOf("煮吃", "洗澡", "洗漱", "厕", "外吃", "热吃", "吃晚饭", "买菜", "晾衣", "拿快递", "理发")
        val family = listOf("老妈", "爷爷")
        val continueWatching = listOf("续观", "续阅", "啊粥", "小吏", "纪史", "卢克文", "差评", "1900", "华商",
            "一知君", "小敏", "程前", "半佛", "思维实验", "许右史", )
        val rest = listOf("睡", "躺歇", "眯躺")
        val exercise = listOf("慢跑", "俯卧撑")
        val communication = listOf("鸿", "铮", "师父", "小洁", "小聚", "聊天", "通话")
        val frame = listOf("财务", "收集库", "时间统计", "清整", "规划", "省思", "统析", "仓库", "预算",
            "纸上统计", "思考", "草思")
        val core = listOf("阅读", "总结", "时光流", "应用", "输出", "初阅", "阅", "搭建个人网站", "渐构",
            "前端学习", "修bug", "HTML和CSS基础学习", "代码")
        val fallow = listOf("散步", "打字", "抽烟", "吹风", "休闲")
        val breach = listOf("躺刷", "贪刷", "朋友圈", "刷视频", "QQ空间", "Q朋抖",
            "刷手机", "躺思", "刷抖音", "动态", "贪观")
        val masturbation = listOf("xxx", "泄", "淫",)
        val getUp = listOf("起床")
        val entertainment = listOf("看电影")

        classifier.apply {
            insert(routine, "常务")
            insert(family, "家人")
            insert(continueWatching, "续观")
            insert(rest, "休息")
            insert(exercise, "锻炼")
            insert(communication, "交际")
            insert(frame, "框架")
            insert(core, "核心")
            insert(fallow, "休闲")
            insert(getUp, "起床")
            insert(breach, "违破")
            insert(masturbation, "xxx")
            insert(entertainment, "娱乐")
        }

        return classifier
    }

}

