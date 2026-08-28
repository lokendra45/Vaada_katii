package com.gaatho.rent.core.auth

import com.skydoves.sandwich.ApiResponse
import kotlinx.serialization.Serializable

/**
 * Interface for modern, seamless authentication using platform-native SDKs
 * like Android Credential Manager.
 */
interface SeamlessAuthHandler {
    /**
     * Attempts to retrieve a verified user profile seamlessly.
     * On Android, this triggers the Credential Manager bottom sheet.
     * If [autoSelect] is true, it may sign in the user without a click.
     */
    suspend fun requestProfile(autoSelect: Boolean = true): ApiResponse<SeamlessProfile>
}

@Serializable
data class SeamlessProfile(
    val email: String,
    val name: String?,
    val pictureUrl: String?,
    val idToken: String? = null
)
