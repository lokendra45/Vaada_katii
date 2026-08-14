package com.gaatho.rent.core.database.di

import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.gaatho.rent.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.dsl.module

val databaseModule = module {
    single<AppDatabase> {
        val builder = get<RoomDatabase.Builder<AppDatabase>>()
        builder.setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .addMigrations(
                AppDatabase.MIGRATION_5_6, 
                AppDatabase.MIGRATION_6_7, 
                AppDatabase.MIGRATION_7_8,
                AppDatabase.MIGRATION_8_9
            )
            .addCallback(object : RoomDatabase.Callback() {
                override suspend fun onOpen(connection: SQLiteConnection) {
                    super.onOpen(connection)
                    connection.execSQL("PRAGMA synchronous = NORMAL")
                    connection.execSQL("PRAGMA busy_timeout = 5000")
                    connection.execSQL("PRAGMA temp_store = MEMORY")
                    connection.execSQL("PRAGMA mmap_size = 134217728")
                }
            })
            .build()
    }
    
    single { get<AppDatabase>().propertyDao() }
    single { get<AppDatabase>().tenantDao() }
    single { get<AppDatabase>().paymentDao() }
    single { get<AppDatabase>().appSettingsDao() }
}

