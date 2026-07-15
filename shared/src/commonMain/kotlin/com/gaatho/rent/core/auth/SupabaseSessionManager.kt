package com.gaatho.rent.core.auth

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
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.jsonPrimitive

/**
 * Implementation of [SessionManager] backed by Supabase Auth (`io.github.jan.supabase.auth`).
 *
 * Observes `supabase.auth.sessionStatus` to provide real-time updates when sessions are
 * restored from local storage, renewed, or terminated (e.g. via token expiration or sign out).
 *
 * @param supabase The configured [SupabaseClient] instance with Auth installed.
 */
class SupabaseSessionManager(
    private val supabase: SupabaseClient
) : SessionManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val currentUser: StateFlow<AuthUser?> = supabase.auth.sessionStatus
        .map { status ->
            when (status) {
                is SessionStatus.Authenticated -> status.session.user?.toAuthUser()
                else -> null
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = supabase.auth.currentUserOrNull()?.toAuthUser()
        )

    override val isLoggedIn: StateFlow<Boolean> = currentUser
        .map { it != null }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = supabase.auth.currentUserOrNull() != null
        )

    override fun currentUserId(): String? = currentUser.value?.id

    /**
     * Maps Supabase [UserInfo] to our clean domain [AuthUser].
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

        return AuthUser(
            id = id,
            email = email ?: "",
            displayName = displayName,
            avatarUrl = metadata?.get("avatar_url")?.jsonPrimitive?.content,
            role = role
        )
    }
}
