package com.gaatho.rent.features.dashboard.di

import com.gaatho.rent.features.dashboard.presentation.MainDashboardViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dashboardModule = module {
    viewModel { MainDashboardViewModel(get()) }
}
