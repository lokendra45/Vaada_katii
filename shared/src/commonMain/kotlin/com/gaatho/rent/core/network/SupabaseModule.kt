package com.gaatho.rent.core.network

import com.gaatho.rent.core.logging.AppLogger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
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
    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = config.url,
            supabaseKey = config.anonKey
        ) {
            httpConfig {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            AppLogger.network.i { message }
                        }
                    }
                    level = LogLevel.ALL
                }
            }
            install(Auth)
            install(Postgrest) {
                serializer = KotlinXSerializer(supabaseJson)
            }
            install(Storage)
            install(Realtime)
        }
    }
    single<Json> { supabaseJson }
}
