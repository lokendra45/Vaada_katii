package com.gaatho.rent.core.utils

expect suspend fun savePdfFile(bytes: ByteArray, fileName: String): Boolean
