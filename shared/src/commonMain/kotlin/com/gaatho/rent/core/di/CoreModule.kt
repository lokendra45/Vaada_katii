package com.gaatho.rent.core.di

import com.gaatho.rent.core.cache.DataStoreCache
import com.gaatho.rent.core.network.ReminderRepository
import org.koin.dsl.module
import kotlinx.serialization.json.Json


val coreModule = module {
    single { DataStoreCache(get(), get()) }
    
    single { 
        ReminderRepository(
            supabase = get(),
            json = get()
        )
    }
}
