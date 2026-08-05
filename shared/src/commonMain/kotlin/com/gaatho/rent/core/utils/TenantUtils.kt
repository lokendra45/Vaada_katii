package com.gaatho.rent.core.utils

import com.gaatho.rent.core.designsystem.ExtendedColorHex

object TenantUtils {

    fun getInitials(name: String): String {
        val parts = name.split(" ").filter { it.isNotBlank() }
        return if (parts.size >= 2) {
            "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
        } else if (parts.size == 1 && parts[0].length >= 2) {
            parts[0].substring(0, 2).uppercase()
        } else {
            name.take(1).uppercase()
        }
    }

    fun getAvatarColors(name: String): Pair<Long, Long> {
        val index = kotlin.math.abs(name.hashCode()) % ExtendedColorHex.AvatarPairs.size
        return ExtendedColorHex.AvatarPairs[index]
    }
}
