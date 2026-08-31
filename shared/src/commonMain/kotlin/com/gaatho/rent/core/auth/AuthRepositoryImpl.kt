package com.gaatho.rent.core.auth

import com.gaatho.rent.core.logging.AppLogger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class AuthRepositoryImpl(
    private val supabase: SupabaseClient
) : AuthRepository {

    // Serializes all auth mutations (sign-in / sign-up / sign-out) so concurrent
    // calls — e.g. a guest sign-out racing a Google sign-in — can't interleave
    // and leave the session half-written.
    private val authMutex = Mutex()

    override suspend fun signInWithEmail(email: String, password: String): com.skydoves.sandwich.ApiResponse<Unit> =
        com.skydoves.sandwich.ApiResponse.suspendOf {
            authMutex.withLock {
                // If the current session is anonymous, sign out first so we get a clean login
                // instead of Supabase trying to link the email identity to the anonymous user.
                signOutAnonymousIfNeeded()
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
            }
        }.also { AppLogger.auth.i { "signInWithEmail result: ${it::class.simpleName}" } }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        role: UserRole
    ): com.skydoves.sandwich.ApiResponse<Boolean> =
        com.skydoves.sandwich.ApiResponse.suspendOf {
            authMutex.withLock {
                signOutAnonymousIfNeeded()
                // signUpWith returns a session only when email confirmation is disabled
                // (auto-confirm). When confirmation is required, it returns null and the
                // user must verify their inbox before a session exists.
                val session = supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    this.data = buildJsonObject {
                        put("role", role.name)
                        put("current_active_role", role.name)
                    }
                }
                session != null
            }
        }.also { AppLogger.auth.i { "signUpWithEmail result: ${it::class.simpleName}" } }

    override suspend fun signOut(): com.skydoves.sandwich.ApiResponse<Unit> =
        com.skydoves.sandwich.ApiResponse.suspendOf {
            authMutex.withLock {
                AppLogger.auth.i { "Signing out..." }
                supabase.auth.signOut()
                AppLogger.auth.i { "Sign out complete" }
            }
        }

    override suspend fun signInWithGoogle(
        role: UserRole?,
        idToken: String?,
        nonce: String?
    ): com.skydoves.sandwich.ApiResponse<Unit> =
        com.skydoves.sandwich.ApiResponse.suspendOf {
            authMutex.withLock {
                if (idToken != null) {
                    // Native sign in with ID token — sign out anonymous first
                    signOutAnonymousIfNeeded()
                    AppLogger.auth.i { "Signing in with Google ID token (native)..." }
                    supabase.auth.signInWith(IDToken) {
                        this.idToken = idToken
                        this.provider = Google
                        this.nonce = nonce
                    }
                    AppLogger.auth.i { "Google ID token sign-in complete" }
                    // Stamp the chosen role on the brand-new user (metadata can't be set
                    // at ID-token sign-in time). Failure here must NOT mask the sign-in.
                    try {
                        ensureUserRole(role ?: UserRole.LANDLORD)
                    } catch (e: Throwable) {
                        AppLogger.auth.w(e) { "Failed to stamp role after native Google sign-in" }
                    }
                } else {
                    // Browser OAuth — sign out anonymous first so Supabase doesn't
                    // perform identity linking (which keeps is_anonymous=true).
                    signOutAnonymousIfNeeded()
                    AppLogger.auth.i { "Starting Google browser OAuth..." }
                    supabase.auth.signInWith(
                        provider = Google,
                        redirectUrl = "com.gaatho.rent://login-callback"
                    )
                    AppLogger.auth.i { "Google browser OAuth initiated (waiting for redirect)" }
                }
            }
        }.also { AppLogger.auth.i { "signInWithGoogle result: ${it::class.simpleName}" } }

    override suspend fun signInAnonymously(): com.skydoves.sandwich.ApiResponse<Unit> =
        com.skydoves.sandwich.ApiResponse.suspendOf {
            authMutex.withLock {
                AppLogger.auth.i { "Signing in anonymously..." }
                supabase.auth.signInAnonymously()
                AppLogger.auth.i { "Anonymous sign-in complete" }
            }
        }

    override suspend fun ensureUserRole(role: UserRole) {
        authMutex.withLock {
            val user = supabase.auth.currentUserOrNull() ?: return
            val currentRole = user.userMetadata?.get("role")?.jsonPrimitive?.content
            if (currentRole.isNullOrBlank()) {
                AppLogger.auth.i { "Stamping role metadata: $role" }
                supabase.auth.updateUser {
                    data = buildJsonObject {
                        put("role", role.name)
                        put("current_active_role", role.name)
                    }
                }
            }
        }
    }

    /**
     * If the current session belongs to an anonymous user, sign them out first.
     *
     * Why: When an anonymous user calls `signInWith(Google)`, Supabase Auth performs
     * an "identity link" — it attaches the Google identity to the existing anonymous
     * user record. The JWT still carries `is_anonymous = true` until a full token
     * refresh cycle. By signing out the anonymous session first, we force a clean
     * sign-in that creates a proper authenticated user from the start.
     */
    private suspend fun signOutAnonymousIfNeeded() {
        val currentUser = supabase.auth.currentUserOrNull()
        if (currentUser?.isAnonymous == true) {
            AppLogger.auth.i { "Current user is anonymous (${currentUser.id}). Signing out before real sign-in." }
            supabase.auth.signOut()
            AppLogger.auth.i { "Anonymous session cleared." }
        }
    }

    override suspend fun updateProfile(name: String, phone: String): com.skydoves.sandwich.ApiResponse<Unit> =
        com.skydoves.sandwich.ApiResponse.suspendOf {
            authMutex.withLock {
                val user = supabase.auth.currentUserOrNull() ?: return@withLock
                val existingData = user.userMetadata ?: buildJsonObject {}
                supabase.auth.updateUser {
                    data = buildJsonObject {
                        existingData.forEach { (k, v) -> put(k, v) }
                        put("full_name", name)
                        put("phone", phone)
                    }
                }
            }
        }
}
