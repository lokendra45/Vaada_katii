package com.gaatho.rent.features.tenant.di

import com.gaatho.rent.features.tenant.data.repository.CloudTenantRepository
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.gaatho.rent.features.tenant.domain.usecase.DeleteTenantUseCase
import com.gaatho.rent.features.tenant.domain.usecase.GetPagedTenantsUseCase
import com.gaatho.rent.features.tenant.domain.usecase.ObserveTenantUseCase
import com.gaatho.rent.features.tenant.domain.usecase.SaveTenantUseCase
import com.gaatho.rent.features.tenant.presentation.details.TenantDetailsViewModel
import com.gaatho.rent.features.tenant.presentation.details.TenantLeaseViewModel
import com.gaatho.rent.features.tenant.presentation.details.TenantProfileViewModel
import com.gaatho.rent.features.tenant.presentation.details.TenantTransactionsViewModel
import com.gaatho.rent.features.tenant.presentation.edit.EditTenantViewModel
import com.gaatho.rent.features.tenant.presentation.list.TenantsListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val tenantModule = module {
    single<TenantRepository> { CloudTenantRepository(get(), get()) }

    factory { ObserveTenantUseCase(get()) }
    factory { GetPagedTenantsUseCase(get()) }
    factory { SaveTenantUseCase(get()) }
    factory { DeleteTenantUseCase(get()) }

    viewModel { TenantsListViewModel(get(), get(), get(), get()) }
    viewModel { params -> TenantDetailsViewModel(tenantId = params.get<String>()) }
    viewModel { params -> TenantProfileViewModel(tenantId = params.get<String>(), get(), get()) }
    viewModel { params -> TenantLeaseViewModel(tenantId = params.get<String>(), get()) }
    viewModel { params -> TenantTransactionsViewModel(tenantId = params.get<String>(), get()) }
    viewModel { params -> EditTenantViewModel(tenantId = params.get<String>(), get(), get(), get(), get(), get()) }
}