package com.gaatho.rent.core.auth

import com.skydoves.sandwich.ApiResponse

/**
 * Repository interface for authentication operations.
 *
 * All operations return Sandwich's [ApiResponse] wrapper to cleanly separate
 * HTTP errors (e.g., 400 Bad Request / Invalid Credentials) from network exceptions
 * (e.g., offline / timeout) without requiring raw try-catch blocks in the ViewModel.
 */
interface AuthRepository {
    /**
     * Signs in an existing user with email and password.
     *
     * @param email User's email address.
     * @param password User's password.
     * @return [ApiResponse.Success] with [Unit] on success, or [ApiResponse.Failure] on error.
     */
    suspend fun signInWithEmail(email: String, password: String): ApiResponse<Unit>

    /**
     * Registers a new account with email, password, display name, and initial role.
     *
     * @param email User's email address.
     * @param password Desired password.
     * @param displayName User's full name.
     * @param role Chosen role ([UserRole.LANDLORD] by default for property owners).
     * @return [ApiResponse.Success] with [Unit] on success, or [ApiResponse.Failure] on error.
     */
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String,
        role: UserRole = UserRole.LANDLORD
    ): ApiResponse<Unit>

    /**
     * Signs out the current user and clears local session storage.
     *
     * @return [ApiResponse.Success] on success, or [ApiResponse.Failure] on error.
     */
    suspend fun signOut(): ApiResponse<Unit>

    /**
     * Sends a password reset recovery email to the specified address.
     *
     * @param email User's email address.
     * @return [ApiResponse.Success] on success, or [ApiResponse.Failure] on error.
     */
    suspend fun resetPassword(email: String): ApiResponse<Unit>

    /**
     * Sends a one-time password (OTP) to the user's email address.
     * If the account does not exist, Supabase will create the user with the specified [role] and [displayName].
     *
     * @param email User's email address.
     * @param displayName User's full name (used if account is created).
     * @param role Chosen role ([UserRole.LANDLORD] or [UserRole.TENANT]).
     */
    suspend fun signInWithOtp(
        email: String,
        displayName: String = "",
        role: UserRole = UserRole.LANDLORD
    ): ApiResponse<Unit>

    /**
     * Verifies the 6-digit one-time code sent to the user's email address.
     *
     * @param email User's email address where the code was sent.
     * @param token The 6-digit verification token entered by the user.
     */
    suspend fun verifyOtp(email: String, token: String): ApiResponse<Unit>

    /**
     * Sends a one-time password (OTP) via SMS to the user's phone number (`+977...`).
     * If the account does not exist, Supabase will create the user (`createUser = true`)
     * and store the selected [role] in user metadata.
     *
     * @param phone Full phone number with E.164 code (e.g. "+9779841234567").
     * @param role Chosen active role ([UserRole.LANDLORD] or [UserRole.TENANT]).
     */
    suspend fun signInWithPhoneOtp(
        phone: String,
        role: UserRole = UserRole.LANDLORD
    ): ApiResponse<Unit>

    /**
     * Verifies the 6-digit verification code sent to the user's phone (`+977...`).
     *
     * @param phone Full phone number where the code was delivered.
     * @param token The 6-digit verification token entered by the user.
     */
    suspend fun verifyPhoneOtp(phone: String, token: String): ApiResponse<Unit>
}
