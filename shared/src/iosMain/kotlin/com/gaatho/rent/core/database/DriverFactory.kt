package com.gaatho.rent.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.gaatho.rent.database.RentManagerDatabase

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(RentManagerDatabase.Schema, "rentmanager.db")
    }
}
