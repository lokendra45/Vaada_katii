package com.gaatho.rent.features.payment.di

import com.gaatho.rent.features.payment.presentation.add.AddPaymentViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val paymentModule = module {
    viewModel { AddPaymentViewModel() }
}
