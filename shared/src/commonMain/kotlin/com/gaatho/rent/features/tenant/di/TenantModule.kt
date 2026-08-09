package com.gaatho.rent.features.tenant.di

import com.gaatho.rent.features.tenant.data.repository.CloudTenantRepository
import com.gaatho.rent.features.tenant.data.repository.LocalTenantRepository
import com.gaatho.rent.features.tenant.data.repository.ProxyTenantRepository
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.gaatho.rent.features.tenant.presentation.add.AddTenantViewModel
import com.gaatho.rent.features.tenant.presentation.list.TenantsListViewModel
import com.gaatho.rent.features.tenant.presentation.edit.EditTenantViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val tenantModule = module {
    single { LocalTenantRepository(get()) }
    single { CloudTenantRepository(get()) }
    single<TenantRepository> { ProxyTenantRepository(get(), get(), get()) }

    viewModel { TenantsListViewModel(get(), get(), get(), get()) }
    viewModel { AddTenantViewModel(get(), get(), get(), get()) }
    viewModel { params -> com.gaatho.rent.features.tenant.presentation.details.TenantDetailsViewModel(tenantId = params.get(), get(), get(), get()) }
    viewModel { params -> EditTenantViewModel(tenantId = params.get(), get(), get(), get()) }
}
