package com.gaatho.rent

import android.app.Application
import com.gaatho.rent.core.network.SupabaseConfig
import com.gaatho.rent.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class RentManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
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
