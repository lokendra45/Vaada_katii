package com.gaatho.rent.core.auth

import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.mapSuccess
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepositoryImpl(
    private val supabase: SupabaseClient
) : AuthRepository {

    override suspend fun signInWithEmail(email: String, password: String): ApiResponse<Unit> =
        ApiResponse.suspendOf {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }.mapSuccess { Unit }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        role: UserRole
    ): ApiResponse<Unit> =
        ApiResponse.suspendOf {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                this.data = buildJsonObject {
                    put("role", role.name)
                    put("current_active_role", role.name)
                }
            }
        }.mapSuccess { Unit }

    override suspend fun signOut(): ApiResponse<Unit> =
        ApiResponse.suspendOf {
            supabase.auth.signOut()
        }.mapSuccess { Unit }

    override suspend fun signInWithGoogle(): ApiResponse<Unit> =
        ApiResponse.suspendOf {
            supabase.auth.signInWith(Google)
        }.mapSuccess { Unit }

    override suspend fun signInAnonymously(): ApiResponse<Unit> =
        ApiResponse.suspendOf {
            supabase.auth.signInAnonymously()
        }.mapSuccess { Unit }
}
