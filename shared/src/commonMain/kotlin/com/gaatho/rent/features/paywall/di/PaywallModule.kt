package com.gaatho.rent.features.paywall.di

import com.gaatho.rent.features.paywall.data.repository.PaywallRepository
import com.gaatho.rent.features.paywall.data.repository.RevenueCatPaywallRepository
import com.gaatho.rent.features.paywall.presentation.PaywallViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val paywallModule = module {
    single<PaywallRepository> { RevenueCatPaywallRepository() }
    viewModel { PaywallViewModel(get()) }
}
