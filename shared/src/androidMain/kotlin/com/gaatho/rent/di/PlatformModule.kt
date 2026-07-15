package com.gaatho.rent.di

import com.gaatho.rent.core.database.DriverFactory
import com.gaatho.rent.core.network.connectivity.AndroidConnectivityObserver
import com.gaatho.rent.core.network.connectivity.ConnectivityObserver
import org.koin.dsl.module

actual val platformModule = module {
    single { DriverFactory(get()) }
    single<ConnectivityObserver> { AndroidConnectivityObserver(get()) }
}
