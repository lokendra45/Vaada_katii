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
     */
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        role: UserRole = UserRole.LANDLORD
    ): ApiResponse<Unit>

    /**
     * Signs out the current user and clears local session storage.
     */
    suspend fun signOut(): ApiResponse<Unit>

    /**
     * Initiates Google OAuth Sign-In.
     */
    suspend fun signInWithGoogle(): ApiResponse<Unit>
}
