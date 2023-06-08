package com.huaguang.flowoftime.di

import android.app.Application
import android.content.Context
import com.huaguang.flowoftime.TimeStreamApplication
import com.huaguang.flowoftime.data.EventDatabase
import com.huaguang.flowoftime.data.EventRepository
import com.huaguang.flowoftime.data.SPHelper
import com.huaguang.flowoftime.data.dao.DateDurationDao
import com.huaguang.flowoftime.data.dao.EventDao
import com.huaguang.flowoftime.ui.components.SharedState
import com.huaguang.flowoftime.utils.AlarmHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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

    @Singleton
    @Provides
    fun provideSPHelper(@ApplicationContext context: Context): SPHelper {
        val sharedPreferences =
            context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        return SPHelper(sharedPreferences)
    }

    @Singleton
    @Provides
    fun provideAlarmHelper(@ApplicationContext context: Context): AlarmHelper {
        return AlarmHelper(context)
    }

    @Singleton
    @Provides
    fun provideCoreSharedState(): SharedState {
        return SharedState()
    }

    // 其他的依赖
}


