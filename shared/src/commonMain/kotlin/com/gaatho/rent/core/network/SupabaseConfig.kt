package com.gaatho.rent.core.network

/**
 * Holds the Supabase project credentials required to initialize the [SupabaseClient].
 *
 * These values must NEVER be hardcoded in source code. They are passed at startup
 * from platform-specific entry points that read from secure build configuration:
 *
 * - **Android**: `BuildConfig.SUPABASE_URL` / `BuildConfig.SUPABASE_KEY`
 *   (populated from `local.properties` via `buildConfigField` in Gradle)
 * - **iOS**: Passed from Swift using Xcode build settings or `.xcconfig` files
 *
 * @property url The full Supabase project URL, e.g. `https://xyzcompany.supabase.co`
 * @property anonKey The Supabase anonymous (public) API key. Safe to use client-side
 *   because Row Level Security enforces data access rules on the server.
 */
data class SupabaseConfig(
    val url: String,
    val anonKey: String,
    /** Web client ID from Google Cloud Console. Required for native Google Sign-In via ComposeAuth. */
    val googleClientId: String = "",
    /** Supabase S3 compatibility credentials for advanced storage operations. */
    val s3SecretKey: String = "",
    val s3AccessKeyId: String = "",
    val s3Endpoint: String = "",
    /**
     * Set to true in debug builds only. When false (production), HTTP logging is disabled
     * to prevent JWT tokens from leaking into device logs.
     */
    val isDebug: Boolean = false
)
