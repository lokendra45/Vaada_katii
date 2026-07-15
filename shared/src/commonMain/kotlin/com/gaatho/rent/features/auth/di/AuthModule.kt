package com.gaatho.rent.features.auth.di

import com.gaatho.rent.core.auth.AuthRepository
import com.gaatho.rent.core.auth.AuthRepositoryImpl
import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.core.auth.SupabaseSessionManager
import com.gaatho.rent.features.auth.presentation.AuthViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for the Authentication feature.
 *
 * Registers:
 * - [SessionManager] → [SupabaseSessionManager] (observes Supabase auth status)
 * - [AuthRepository] → [AuthRepositoryImpl] (Sandwich-wrapped auth calls)
 * - [AuthViewModel] with [SavedStateHandle] injection
 */
val authModule = module {
    single<SessionManager> { SupabaseSessionManager(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    viewModel { AuthViewModel(get(), get()) }
}
