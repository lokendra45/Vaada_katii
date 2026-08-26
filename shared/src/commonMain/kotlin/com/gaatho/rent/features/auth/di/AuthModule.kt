package com.gaatho.rent.features.auth.di

import com.gaatho.rent.core.auth.AuthRepository
import com.gaatho.rent.core.auth.AuthRepositoryImpl
import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.core.auth.SupabaseSessionManager
import com.gaatho.rent.features.auth.presentation.AuthViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single<SessionManager> { SupabaseSessionManager(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    viewModel { AuthViewModel(get(), get()) }
}
