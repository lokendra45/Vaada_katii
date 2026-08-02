package com.gaatho.rent.di

import android.content.Context
import com.gaatho.rent.core.database.DriverFactory
import com.gaatho.rent.core.network.connectivity.AndroidConnectivityObserver
import com.gaatho.rent.core.network.connectivity.ConnectivityObserver
import com.gaatho.rent.core.environment.createDataStore
import org.koin.dsl.module

actual val platformModule = module {
    single { DriverFactory(get()) }
    single<ConnectivityObserver> { AndroidConnectivityObserver(get()) }
    single { createDataStore(get<Context>()) }
}
