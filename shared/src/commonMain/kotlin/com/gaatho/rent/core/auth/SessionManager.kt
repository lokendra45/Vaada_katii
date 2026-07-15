package com.gaatho.rent.core.auth

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface managing the current authentication session state across the application.
 *
 * This provides a reactive, single source of truth for whether the user is logged in
 * and who they are. All feature viewmodels and navigation graphs should observe this
 * to reactively gate routes or filter data (e.g. by `currentUserId()`).
 */
interface SessionManager {
    /**
     * Cold or stateful stream emitting the currently authenticated [AuthUser], or `null` if unauthenticated.
     */
    val currentUser: StateFlow<AuthUser?>

    /**
     * Stream emitting `true` when a valid session exists, `false` otherwise.
     */
    val isLoggedIn: StateFlow<Boolean>

    /**
     * Synchronously returns the current user's ID (`auth.users.id`), or `null` if not logged in.
     */
    fun currentUserId(): String?
}
