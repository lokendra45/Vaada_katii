package com.gaatho.rent

import android.app.Application
import com.gaatho.rent.core.network.SupabaseConfig
import com.gaatho.rent.di.initKoin
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class RentManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize RevenueCat — must be done before any Purchases API call
        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(
            PurchasesConfiguration.Builder(BuildConfig.REVENUECAT_API_KEY).build()
        )

        initKoin(
            supabaseConfig = SupabaseConfig(
                url = BuildConfig.SUPABASE_URL,
                anonKey = BuildConfig.SUPABASE_KEY
            )
        ) {
            androidLogger()
            androidContext(this@RentManagerApplication)
        }
    }
}
