package com.gaatho.rent.core.auth

/**
 * Represents the application-level authentication state, strictly derived from Supabase.
 */
sealed interface AuthState {
    
    /**
     * The initial state while Supabase is restoring the session from local storage.
     * The application root should wait in this state before deciding to navigate to the 
     * main app or the login screen to prevent flickering.
     */
    data object Loading : AuthState

    /**
     * A valid session exists for an anonymous user (guest).
     */
    data class Anonymous(val user: AuthUser) : AuthState

    /**
     * A valid session exists for a fully registered user (e.g. Email/Password, Google).
     */
    data class Authenticated(val user: AuthUser) : AuthState

    /**
     * No valid session exists.
     */
    data object Unauthenticated : AuthState
}
