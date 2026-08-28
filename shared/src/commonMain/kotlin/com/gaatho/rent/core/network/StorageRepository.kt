package com.gaatho.rent.core.network

import com.skydoves.sandwich.ApiResponse

interface StorageRepository {
    suspend fun uploadFile(bucket: String, path: String, fileBytes: ByteArray): ApiResponse<String>
}
