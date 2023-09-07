package com.huaguang.flowoftime.di

import android.app.Application
import android.content.Context
import com.huaguang.flowoftime.TimeStreamApplication
import com.huaguang.flowoftime.data.dao.DateDurationDao
import com.huaguang.flowoftime.data.dao.EventDao
import com.huaguang.flowoftime.data.dao.IconMappingDao
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.repositories.IconMappingRepository
import com.huaguang.flowoftime.data.sources.DataStoreHelper
import com.huaguang.flowoftime.data.sources.EventDatabase
import com.huaguang.flowoftime.data.sources.IconDatabase
import com.huaguang.flowoftime.data.sources.SPHelper
import com.huaguang.flowoftime.other.LocalDateTimeSerializer
import com.huaguang.flowoftime.ui.components.SharedState
import com.huaguang.flowoftime.utils.AlarmHelper
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
    fun provideEventDB(application: Application): EventDatabase {
        return (application as TimeStreamApplication).eventDB
    }

    @Singleton
    @Provides
    fun provideIconDB(application: Application): IconDatabase {
        return (application as TimeStreamApplication).iconDB
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
    fun provideIconMappingDao(database: IconDatabase): IconMappingDao {
        return database.iconMappingDao()
    }

    @Singleton
    @Provides
    fun provideEventRepository(eventDao: EventDao, dateDurationDao: DateDurationDao): EventRepository {
        return EventRepository(eventDao, dateDurationDao)
    }

    @Singleton
    @Provides
    fun provideIconMappingRepository(dao: IconMappingDao): IconMappingRepository {
        return IconMappingRepository(dao)
    }

    @Singleton
    @Provides
    fun provideSPHelper(@ApplicationContext context: Context): SPHelper {
        return SPHelper.getInstance(context)
    }

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


