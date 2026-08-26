package com.gaatho.rent.features.payment.di

import com.gaatho.rent.features.payment.data.repository.CloudPaymentRepository
import com.gaatho.rent.features.payment.domain.repository.PaymentRepository
import com.gaatho.rent.features.payment.presentation.add.AddPaymentViewModel
import com.gaatho.rent.features.payment.presentation.details.PaymentDetailsViewModel
import com.gaatho.rent.features.payment.presentation.edit.EditPaymentViewModel
import com.gaatho.rent.features.payment.presentation.list.PaymentListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val paymentModule = module {
    single<PaymentRepository> { CloudPaymentRepository(get(), get(), get()) }

    viewModel { AddPaymentViewModel(get(), get(), get(), get()) }
    viewModel { PaymentListViewModel(get(), get()) }
    viewModel { parameters -> PaymentDetailsViewModel(parameters.get(), get(), get(), get(), get()) }
    viewModel { parameters -> EditPaymentViewModel(parameters.get(), get(), get(), get(), get()) }
}
