package com.huaguang.flowoftime.di

import android.app.Application
import android.content.Context
import com.huaguang.flowoftime.TimeStreamApplication
import com.huaguang.flowoftime.data.dao.DateDurationDao
import com.huaguang.flowoftime.data.dao.EventDao
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.sources.DataStoreHelper
import com.huaguang.flowoftime.data.sources.EventDatabase
import com.huaguang.flowoftime.ui.components.SharedState
import com.huaguang.flowoftime.utils.AlarmHelper
import com.huaguang.flowoftime.utils.LocalDateTimeSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.time.LocalDateTime
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(application: Application): EventDatabase {
        return (application as TimeStreamApplication).database
    }

    @Singleton
    @Provides
    fun provideEventDao(database: EventDatabase): EventDao {
        return database.eventDao()
    }

    @Singleton
    @Provides
    fun provideDateDurationDao(database: EventDatabase): DateDurationDao {
        return database.dateDurationDao()
    }

    @Singleton
    @Provides
    fun provideEventRepository(eventDao: EventDao, dateDurationDao: DateDurationDao): EventRepository {
        return EventRepository(eventDao, dateDurationDao)
    }

//    @Singleton
//    @Provides
//    fun provideSPHelper(@ApplicationContext context: Context): SPHelper {
//        val sharedPreferences =
//            context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
//        return SPHelper(sharedPreferences)
//    }

    @Singleton
    @Provides
    fun provideJson(): Json {
        return Json {
            serializersModule = SerializersModule {
                contextual(LocalDateTime::class, LocalDateTimeSerializer)
            }
        }
    }

    @Singleton
    @Provides
    fun provideDataStoreHelper(
        @ApplicationContext context: Context,
        json: Json
    ): DataStoreHelper {
        return DataStoreHelper(context, json)
    }

    @Singleton
    @Provides
    fun provideAlarmHelper(@ApplicationContext context: Context): AlarmHelper {
        return AlarmHelper(context)
    }

    @Singleton
    @Provides
    fun provideSharedState(application: Application): SharedState {
        return SharedState(application)
    }



    // 其他的依赖
}


