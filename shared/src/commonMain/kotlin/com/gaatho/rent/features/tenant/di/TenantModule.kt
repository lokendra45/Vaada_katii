package com.gaatho.rent.features.tenant.di

import com.gaatho.rent.features.tenant.data.repository.CloudTenantRepository
import com.gaatho.rent.features.tenant.data.repository.LocalTenantRepository
import com.gaatho.rent.features.tenant.data.repository.ProxyTenantRepository
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.gaatho.rent.features.tenant.presentation.list.TenantsListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val tenantModule = module {
    single { LocalTenantRepository(get()) }
    single { CloudTenantRepository(get()) }
    single<TenantRepository> { ProxyTenantRepository(get(), get(), get()) }

    viewModel { TenantsListViewModel(get(), get(), get(), get()) }
}
