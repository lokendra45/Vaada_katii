package com.gaatho.rent.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Embedded

data class TenantWithPropertyName(
    @Embedded val tenant: TenantEntity,
    @ColumnInfo(name = "property_name")
    val propertyName: String?
)
