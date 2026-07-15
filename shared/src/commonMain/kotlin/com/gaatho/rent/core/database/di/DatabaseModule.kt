package com.gaatho.rent.core.database.di

import com.gaatho.rent.core.database.DriverFactory
import com.gaatho.rent.database.RentManagerDatabase
import org.koin.dsl.module

val databaseModule = module {
    single {
        val driver = get<DriverFactory>().createDriver()
        RentManagerDatabase(driver)
    }
}
