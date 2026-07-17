package com.gaatho.rent.di

import com.gaatho.rent.core.database.di.databaseModule
import com.gaatho.rent.core.network.SupabaseConfig
import com.gaatho.rent.core.network.supabaseModule

import com.gaatho.rent.features.auth.di.authModule
import com.gaatho.rent.features.dashboard.di.dashboardModule
import com.gaatho.rent.features.paywall.di.paywallModule
import com.gaatho.rent.features.property.di.propertyModule
import com.gaatho.rent.features.splash.di.splashModule
import com.gaatho.rent.features.tenant.di.tenantModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Initializes Koin with all application modules.
 *
 * [SupabaseConfig] is passed explicitly to keep API credentials out of source code.
 * On Android, values are read from BuildConfig (populated via local.properties).
 * On iOS, values are passed from Swift/Xcode build settings.
 *
 * @param supabaseConfig Supabase project URL and anon key.
 * @param appDeclaration Platform-specific Koin configuration (e.g. androidContext, androidLogger).
 */
fun initKoin(supabaseConfig: SupabaseConfig, appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(
            platformModule,
            databaseModule,

            supabaseModule(supabaseConfig),
            splashModule,
            authModule,
            propertyModule,
            dashboardModule,
            paywallModule,
            tenantModule,
            // Add feature modules here as they are built
        )
    }


/** Convenience overload for iOS entry point — supply real keys from Xcode build settings. */
fun initKoin(
    supabaseUrl: String,
    supabaseAnonKey: String
) = initKoin(SupabaseConfig(url = supabaseUrl, anonKey = supabaseAnonKey))
