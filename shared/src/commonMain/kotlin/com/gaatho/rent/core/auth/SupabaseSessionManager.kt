package com.gaatho.rent.core.auth

import com.gaatho.rent.core.logging.AppLogger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.jsonPrimitive

/**
 * Production implementation of [SessionManager] backed by Supabase Auth.
 *
 * Maps `supabase.auth.sessionStatus` → [AuthState] in real time.
 * This is a singleton — it survives across ViewModel lifetimes.
 */
class SupabaseSessionManager(
    private val supabase: SupabaseClient
) : SessionManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val authState: StateFlow<AuthState> = supabase.auth.sessionStatus
        .onEach { status ->
            AppLogger.auth.i { "SessionStatus → ${status::class.simpleName}" }
        }
        .map { status ->
            when (status) {
                is SessionStatus.Initializing -> AuthState.Loading

                is SessionStatus.RefreshFailure -> {
                    // A transient refresh failure (e.g. a one-off network blip) should not
                    // violently log the user out. Supabase retries the refresh automatically,
                    // so if a valid session still exists we stay in Loading and let the retry
                    // resolve to Authenticated. Only go to Unauthenticated when there is truly
                    // no session left.
                    AppLogger.auth.w { "Token refresh failed; waiting for retry" }
                    if (supabase.auth.currentSessionOrNull() != null) AuthState.Loading
                    else AuthState.Unauthenticated
                }

                is SessionStatus.Authenticated -> {
                    val user = status.session.user
                    if (user == null) {
                        AppLogger.auth.w { "Authenticated status but user is null" }
                        AuthState.Unauthenticated
                    } else {
                        val authUser = user.toAuthUser()
                        // Avoid logging PII (email, identity providers) in production.
                        AppLogger.auth.i {
                            "Session established: id=${authUser.id}, isAnonymous=${authUser.isAnonymous}"
                        }
                        if (authUser.isAnonymous) AuthState.Anonymous(authUser)
                        else AuthState.Authenticated(authUser)
                    }
                }

                is SessionStatus.NotAuthenticated -> AuthState.Unauthenticated
            }
        }
        .onEach { state ->
            AppLogger.auth.i { "AuthState → ${state::class.simpleName}" }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = AuthState.Loading as AuthState
        )

    override fun currentUserId(): String? {
        return when (val state = authState.value) {
            is AuthState.Authenticated -> state.user.id
            is AuthState.Anonymous -> state.user.id
            else -> null
        }
    }

    /**
     * Maps Supabase [UserInfo] to our domain [AuthUser].
     *
     * Anonymous detection: A user is truly anonymous only if `isAnonymous == true`
     * AND they have no real (non-anonymous) identity provider attached.
     * This handles the edge case where Supabase keeps `isAnonymous = true` after
     * identity linking until the next token refresh.
     */
    private fun UserInfo.toAuthUser(): AuthUser {
        val metadata = userMetadata
        val displayName = metadata?.get("display_name")?.jsonPrimitive?.content
            ?: metadata?.get("full_name")?.jsonPrimitive?.content

        val roleStr = metadata?.get("role")?.jsonPrimitive?.content?.uppercase()
        val role = when (roleStr) {
            "TENANT" -> UserRole.TENANT
            else -> UserRole.LANDLORD
        }

        // A user is only truly anonymous if:
        // 1. Supabase says isAnonymous == true
        // 2. They have no real identity (e.g. Google, email) attached
        // 3. They have no email
        val hasRealIdentity = identities?.any { it.provider != "anonymous" } == true
        val trulyAnonymous = isAnonymous == true && email.isNullOrBlank() && !hasRealIdentity

        return AuthUser(
            id = id,
            email = email ?: "",
            displayName = displayName,
            avatarUrl = metadata?.get("avatar_url")?.jsonPrimitive?.content,
            role = role,
            isAnonymous = trulyAnonymous
        )
    }
}
