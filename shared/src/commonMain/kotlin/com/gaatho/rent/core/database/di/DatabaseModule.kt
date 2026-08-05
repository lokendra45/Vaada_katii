package com.gaatho.rent.core.database.di

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.gaatho.rent.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.dsl.module

val databaseModule = module {
    single<AppDatabase> {
        val builder = get<RoomDatabase.Builder<AppDatabase>>()
        builder.setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
    
    single { get<AppDatabase>().propertyDao() }
    single { get<AppDatabase>().tenantDao() }
    single { get<AppDatabase>().paymentDao() }
    single { get<AppDatabase>().appSettingsDao() }
}

