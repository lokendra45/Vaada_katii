package com.gaatho.rent.core.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.gaatho.rent.database.RentManagerDatabase

actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(RentManagerDatabase.Schema, context, "rentmanager.db")
    }
}
