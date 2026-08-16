package com.gaatho.rent.features.dashboard.di

import com.gaatho.rent.features.dashboard.data.CloudDashboardRepository
import com.gaatho.rent.features.dashboard.data.DashboardRepository
import com.gaatho.rent.features.dashboard.presentation.MainDashboardViewModel
import com.gaatho.rent.features.dashboard.presentation.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dashboardModule = module {
    single<DashboardRepository> { CloudDashboardRepository(get(), get()) }
    viewModel { MainDashboardViewModel(get()) }
    viewModel { HomeViewModel(get(), get()) }
}
