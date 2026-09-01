package com.gaatho.rent.core.auth

import com.skydoves.sandwich.ApiResponse

/**
 * Repository interface for authentication operations.
 *
 * All operations return Sandwich's [ApiResponse] wrapper to cleanly separate
 * HTTP errors from network exceptions.
 */
interface AuthRepository {
    /**
     * Signs in an existing user with email and password.
     */
    suspend fun signInWithEmail(email: String, password: String): ApiResponse<Unit>

    /**
     * Registers a new account with email and password.
     * Returns [ApiResponse.Success] with `true` if a session was created immediately
     * (auto-confirm enabled) or `false` when email confirmation is required and the
     * user must verify their inbox before a session exists.
     */
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        role: UserRole = UserRole.LANDLORD
    ): ApiResponse<Boolean>

    /**
     * Ensures the authenticated user's `role` (and `current_active_role`) metadata is set.
     * Only writes when the metadata is currently absent, so a returning user's existing
     * role is never overwritten. Used to stamp the role chosen on the login screen for
     * new social sign-ins (Google) that cannot carry metadata at sign-in time.
     */
    suspend fun ensureUserRole(role: UserRole)

    /**
     * Signs in as an anonymous guest user.
     */
    suspend fun signInAnonymously(): ApiResponse<Unit>

    /**
     * Signs out the current user and clears local session storage.
     */
    suspend fun signOut(): ApiResponse<Unit>

    /**
     * Initiates Google OAuth Sign-In.
     * If [idToken] is provided, it uses native sign-in.
     */
    suspend fun signInWithGoogle(
        role: UserRole? = null,
        idToken: String? = null,
        nonce: String? = null
    ): ApiResponse<Unit>

    /**
     * Updates the user's profile information (name, phone).
     */
    suspend fun updateProfile(name: String, phone: String): ApiResponse<Unit>

    /**
     * Updates the user's avatar URL.
     */
    suspend fun updateAvatarUrl(url: String): ApiResponse<Unit>
}
