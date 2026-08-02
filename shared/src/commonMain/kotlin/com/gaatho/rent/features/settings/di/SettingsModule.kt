package com.gaatho.rent.features.settings.di

import com.gaatho.rent.features.settings.presentation.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    viewModel {
        SettingsViewModel(
            authRepository = get(),
            sessionManager = get(),
            database = get()
        )
    }
}
