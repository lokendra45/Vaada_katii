package com.gaatho.rent.di

import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.gaatho.rent.database.AppDatabase
import com.gaatho.rent.core.network.connectivity.ConnectivityObserver
import com.gaatho.rent.core.network.connectivity.IosConnectivityObserver
import com.gaatho.rent.core.environment.createDataStore
import com.gaatho.rent.core.security.BiometricAuthenticator
import com.gaatho.rent.core.security.IosBiometricAuthenticator
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.dsl.module
import platform.Foundation.NSHomeDirectory

actual val platformModule = module {
    single<RoomDatabase.Builder<AppDatabase>> { 
        val dbFilePath = NSHomeDirectory() + "/rent_manager.db"
        Room.databaseBuilder<AppDatabase>(
            name = dbFilePath
        )
    }
    single<ConnectivityObserver> { IosConnectivityObserver() }
    single<DataStore<Preferences>> { createDataStore() }
    single<BiometricAuthenticator> { IosBiometricAuthenticator() }
}

