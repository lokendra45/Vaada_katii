package com.gaatho.rent.core.security.di

import com.gaatho.rent.core.security.presentation.SecurityViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val securityModule = module {
    viewModel {
        SecurityViewModel(
            dataStore = get(),
            authenticator = get()
        )
    }
}
