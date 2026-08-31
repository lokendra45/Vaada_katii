package com.gaatho.rent.features.settings.di

import com.gaatho.rent.features.settings.presentation.SettingsViewModel
import com.gaatho.rent.core.environment.LanguageViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    viewModel {
        SettingsViewModel(
            authRepository = get(),
            sessionManager = get(),
            dataStore = get(),
            authenticator = get(),
            supabase = get()
        )
    }
    viewModel {
        LanguageViewModel(
            dataStore = get()
        )
    }
}
