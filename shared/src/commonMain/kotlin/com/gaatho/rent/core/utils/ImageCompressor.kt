package com.gaatho.rent.core.utils

expect suspend fun compressImage(imageBytes: ByteArray, fileName: String, compressionThreshold: Long = 150_000L): ByteArray
