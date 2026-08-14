package com.gaatho.rent.di

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.gaatho.rent.database.AppDatabase
import com.gaatho.rent.core.network.connectivity.AndroidConnectivityObserver
import com.gaatho.rent.core.network.connectivity.ConnectivityObserver
import com.gaatho.rent.core.environment.createDataStore
import com.gaatho.rent.core.security.AndroidBiometricAuthenticator
import com.gaatho.rent.core.security.BiometricAuthenticator
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.dsl.module

actual val platformModule = module {
    single<RoomDatabase.Builder<AppDatabase>> { 
        val context = get<Context>()
        val dbFile = context.getDatabasePath("rent_manager.db")
        Room.databaseBuilder<AppDatabase>(
            context = context.applicationContext,
            name = dbFile.absolutePath
        ).setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
         .fallbackToDestructiveMigrationOnDowngrade()
    }
    single<ConnectivityObserver> { AndroidConnectivityObserver(get()) }
    single<DataStore<Preferences>> { createDataStore(get<Context>()) }
    single<BiometricAuthenticator> { AndroidBiometricAuthenticator() }
}

