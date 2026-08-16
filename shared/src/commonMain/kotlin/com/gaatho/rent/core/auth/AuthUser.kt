package com.gaatho.rent.core.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Role of the user within Rent Manager Nepal.
 */
@Serializable
enum class UserRole {
    @SerialName("LANDLORD")
    LANDLORD,

    @SerialName("TENANT")
    TENANT
}

/**
 * Domain model representing an authenticated user in Rent Manager Nepal.
 *
 * @property id Unique Supabase user UUID (`auth.users.id`).
 * @property email User's email address.
 * @property displayName User's full name or display name.
 * @property avatarUrl Optional profile image URL from Supabase Storage.
 * @property role Whether the user is a Landlord managing properties or a Tenant renting a room/flat.
 */
@Serializable
data class AuthUser(
    @SerialName("id") val id: String,
    @SerialName("email") val email: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("role") val role: UserRole = UserRole.LANDLORD,
    @SerialName("is_anonymous") val isAnonymous: Boolean = false
)
