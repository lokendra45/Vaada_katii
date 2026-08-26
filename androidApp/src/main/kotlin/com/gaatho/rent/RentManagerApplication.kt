package com.gaatho.rent

import android.app.Application
import android.util.Log
import com.gaatho.rent.core.network.SupabaseConfig
import com.gaatho.rent.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class RentManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        installGlobalCrashHandler()

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

    /**
     * Logs every uncaught thread crash instead of letting the OS show a raw
     * "app keeps stopping" dialog with an unreadable stack trace. The default
     * handler is still chained so platform crash reporting can run after us.
     */
    private fun installGlobalCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e("RentManager-Crash", "Uncaught exception on thread ${thread.name}", throwable)
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
