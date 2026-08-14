package com.gaatho.rent.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.gaatho.rent.database.dao.AppSettingsDao
import com.gaatho.rent.database.dao.PaymentDao
import com.gaatho.rent.database.dao.PropertyDao
import com.gaatho.rent.database.dao.TenantDao
import com.gaatho.rent.database.entity.AppSettingsEntity
import com.gaatho.rent.database.entity.PaymentEntity
import com.gaatho.rent.database.entity.PropertyEntity
import com.gaatho.rent.database.entity.TenantEntity

import androidx.room3.ColumnTypeConverters
import com.gaatho.rent.database.converter.EncryptedStringConverter

import androidx.room3.ConstructedBy
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.AutoMigration
import com.gaatho.rent.database.converter.StringCollectionConverter

@Database(
    entities = [PropertyEntity::class, TenantEntity::class, PaymentEntity::class, AppSettingsEntity::class],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4)
    ]
)
@ColumnTypeConverters(EncryptedStringConverter::class, StringCollectionConverter::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun propertyDao(): PropertyDao
    abstract fun tenantDao(): TenantDao
    abstract fun paymentDao(): PaymentDao
    abstract fun appSettingsDao(): AppSettingsDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
