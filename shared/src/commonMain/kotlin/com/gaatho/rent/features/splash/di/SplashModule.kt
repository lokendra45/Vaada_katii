package com.gaatho.rent.features.splash.di

import com.gaatho.rent.features.splash.presentation.SplashViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for the Splash screen feature.
 */
val splashModule = module {
    viewModel { SplashViewModel(get(), get()) }
}
