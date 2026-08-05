package com.gaatho.rent.database.converter

import androidx.room3.ColumnTypeConverter

class StringCollectionConverter {
    @ColumnTypeConverter
    fun fromStringSet(set: Set<String>?): String? {
        return set?.joinToString(",")
    }

    @ColumnTypeConverter
    fun toStringSet(data: String?): Set<String>? {
        return data?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet()
    }
}
