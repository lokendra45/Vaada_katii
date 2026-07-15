package com.gaatho.rent.di

import com.gaatho.rent.core.database.DriverFactory
import com.gaatho.rent.core.store.SqlDelightBookkeeper
import com.gaatho.rent.database.RentManagerDatabase
import org.koin.dsl.module
import org.mobilenativefoundation.store.store5.Bookkeeper

/**
 * Provides the Store5 [Bookkeeper] backed by SQLDelight.
 *
 * Separated from [databaseModule] to keep database creation concerns distinct from
 * Store5 infrastructure concerns. This module is shared across all features.
 *
 * Note: Feature-specific Stores and Repositories belong in their own feature modules
 * (e.g. [propertyModule]), NOT here.
 */
val databaseStoreModule = module {
    single<Bookkeeper<String>> { SqlDelightBookkeeper(get()) }
}
