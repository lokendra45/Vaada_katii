package com.gaatho.rent.di

import com.gaatho.rent.core.database.DriverFactory
import com.gaatho.rent.core.network.connectivity.ConnectivityObserver
import com.gaatho.rent.core.network.connectivity.IosConnectivityObserver
import com.gaatho.rent.core.environment.createDataStore
import com.gaatho.rent.core.security.BiometricAuthenticator
import com.gaatho.rent.core.security.IosBiometricAuthenticator
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.dsl.module

actual val platformModule = module {
    single { DriverFactory() }
    single<ConnectivityObserver> { IosConnectivityObserver() }
    single<DataStore<Preferences>> { createDataStore() }
    single<BiometricAuthenticator> { IosBiometricAuthenticator() }
}
