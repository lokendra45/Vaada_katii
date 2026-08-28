package com.gaatho.rent.core.network

import com.skydoves.sandwich.ApiResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class SupabaseStorageRepository(
    private val supabase: SupabaseClient
) : StorageRepository {
    override suspend fun uploadFile(bucket: String, path: String, fileBytes: ByteArray): ApiResponse<String> =
        ApiResponse.suspendOf {
            val storageBucket = supabase.storage.from(bucket)
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("User must be logged in to upload files")
                
            val securePath = "$userId/$path"
            
            storageBucket.upload(securePath, fileBytes) {
                upsert = true
                httpOverride {
                    headers["cache-control"] = "max-age=31536000"
                    
                    val extension = path.substringAfterLast('.', "").lowercase()
                    val mimeType = when (extension) {
                        "jpg", "jpeg" -> "image/jpeg"
                        "png" -> "image/png"
                        "webp" -> "image/webp"
                        "pdf" -> "application/pdf"
                        else -> "application/octet-stream"
                    }
                    headers["Content-Type"] = mimeType
                }
            }
            
            if (bucket == "documents") {
                // Documents is private, create a signed URL valid for 24h
                storageBucket.createSignedUrl(securePath, expiresIn = 86400.toDuration(DurationUnit.SECONDS))
            } else {
                storageBucket.publicUrl(securePath)
            }
        }
}
