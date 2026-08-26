package com.gaatho.rent.features.paywall.di

import com.gaatho.rent.features.paywall.data.repository.PaywallRepository
import com.gaatho.rent.features.paywall.data.repository.StubPaywallRepository
import com.gaatho.rent.features.paywall.presentation.PaywallViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val paywallModule = module {
    // Stub until WebView-based eSewa/Khalti payment flow is implemented.
    // Replace StubPaywallRepository with a real implementation once the
    // payment gateway integration and Supabase verification are ready.
    single<PaywallRepository> { StubPaywallRepository() }
    viewModel { PaywallViewModel(get()) }
}
