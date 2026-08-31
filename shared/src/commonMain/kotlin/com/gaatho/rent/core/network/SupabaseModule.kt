package com.gaatho.rent.core.network

import com.gaatho.rent.core.logging.AppLogger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import kotlinx.serialization.json.Json
import org.koin.dsl.module

/**
 * Lenient JSON config shared by all Supabase serialization. Unknown keys
 * (e.g. embedded `property(name)` joins) are ignored instead of failing.
 */
internal val supabaseJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

/**
 * Creates a Koin module that provides a [SupabaseClient] configured with the
 * given [SupabaseConfig].
 *
 * Using a function (rather than a top-level `val module`) allows the credentials
 * to be injected at startup time from platform-specific build config, keeping
 * them out of source code entirely.
 *
 * @param config The Supabase project URL and anon key.
 */
@OptIn(io.github.jan.supabase.annotations.SupabaseInternal::class, io.github.jan.supabase.annotations.SupabaseExperimental::class)
fun supabaseModule(config: SupabaseConfig) = module {
    single { config }
    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = config.url,
            supabaseKey = config.anonKey
        ) {
            httpConfig {
                if (config.isDebug) {
                    install(Logging) {
                        logger = object : Logger {
                            override fun log(message: String) {
                                AppLogger.network.i { message }
                            }
                        }
                        // HEADERS only — avoids logging request bodies that may contain sensitive data
                        level = LogLevel.HEADERS
                    }
                }
                // In production (isDebug=false), no Logging plugin is installed.
                // This prevents JWT tokens and request bodies from leaking to logcat.
            }
            install(Auth) {
                // Must match the OAuth deep-link intent-filter in AndroidManifest.xml so that
                // supabase.handleDeeplinks() accepts and imports the browser redirect.
                // PKCE is used (more secure than IMPLICIT); the code verifier is persisted via the
                // default SettingsCodeVerifierCache (backed by platform Settings/SharedPreferences),
                // so a cold-start relaunch during OAuth still completes the code exchange.
                flowType = FlowType.PKCE
                scheme = "com.gaatho.rent"
                host = "login-callback"
            }
            install(Postgrest) {
                serializer = KotlinXSerializer(supabaseJson)
            }
            install(Storage)
            install(Realtime)
            install(io.github.jan.supabase.functions.Functions)

            // ComposeAuth — handles native Google Sign-In via Credential Manager on Android.
            // googleClientId is the *Web* OAuth client ID from Google Cloud Console.
            // Ignored on iOS (empty string → fallback to browser OAuth).
            if (config.googleClientId.isNotBlank()) {
                install(ComposeAuth) {
                    googleNativeLogin(serverClientId = config.googleClientId)
                }
            }
        }
    }
    single<Json> { supabaseJson }
}
