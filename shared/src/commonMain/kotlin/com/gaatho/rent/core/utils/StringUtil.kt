package com.gaatho.rent.core.utils

/**
 * Common string utilities for database search and normalization.
 */
object StringUtil {

    /**
     * Escapes SQLite/Postgres LIKE wildcard characters (% and _).
     * Usage: `LOWER(name) LIKE '%' || :query || '%' ESCAPE '\'`
     */
    fun escapeLike(query: String): String {
        return query
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
    }

    /**
     * Normalizes search query: trim, remove multiple spaces, and limit length.
     */
    fun normalizeSearch(query: String, maxLength: Int = 100): String {
        val trimmed = query.trim()
        val singleSpaced = trimmed.replace("\\s+".toRegex(), " ")
        return singleSpaced.take(maxLength)
    }
}
