package com.gaatho.rent.di

import android.content.Context
import com.gaatho.rent.core.network.connectivity.AndroidConnectivityObserver
import com.gaatho.rent.core.network.connectivity.ConnectivityObserver
import com.gaatho.rent.core.environment.createDataStore
import com.gaatho.rent.core.notifications.AndroidNotificationService
import com.gaatho.rent.core.notifications.NotificationService
import com.gaatho.rent.core.security.AndroidBiometricAuthenticator
import com.gaatho.rent.core.security.BiometricAuthenticator
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.dsl.module

actual val platformModule = module {
    single<ConnectivityObserver> { AndroidConnectivityObserver(get()) }
    single<DataStore<Preferences>> { createDataStore(get<Context>()) }
    single<BiometricAuthenticator> { AndroidBiometricAuthenticator() }
    single<NotificationService> { AndroidNotificationService(get()) }
    single { get<android.content.Context>().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }
}
