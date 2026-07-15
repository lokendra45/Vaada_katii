package com.gaatho.rent.di

import com.gaatho.rent.core.database.DriverFactory
import com.gaatho.rent.core.network.connectivity.ConnectivityObserver
import com.gaatho.rent.core.network.connectivity.IosConnectivityObserver
import org.koin.dsl.module

actual val platformModule = module {
    single { DriverFactory() }
    single<ConnectivityObserver> { IosConnectivityObserver() }
}
