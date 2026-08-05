package com.gaatho.rent.features.payment.di

import com.gaatho.rent.features.payment.presentation.add.AddPaymentViewModel
import com.gaatho.rent.features.payment.presentation.list.PaymentsListViewModel
import com.gaatho.rent.features.payment.presentation.details.PaymentDetailsViewModel
import com.gaatho.rent.features.payment.data.repository.CloudPaymentRepository
import com.gaatho.rent.features.payment.data.repository.LocalPaymentRepository
import com.gaatho.rent.features.payment.data.repository.ProxyPaymentRepository
import com.gaatho.rent.features.payment.domain.repository.PaymentRepository
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val paymentModule = module {
    single { LocalPaymentRepository(get()) }
    single { CloudPaymentRepository(get()) }
    single<PaymentRepository> { 
        ProxyPaymentRepository(
            local = get(),
            cloud = get(),
            sessionManager = get()
        )
    }

    viewModel { AddPaymentViewModel(get(), get(), get(), get()) }
    viewModel { PaymentsListViewModel(get(), get(), get(), get(), get()) }
    viewModel { parameters -> PaymentDetailsViewModel(parameters.get(), get(), get(), get(), get()) }
}
