package com.gaatho.rent.features.property.di

import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.property.data.repository.CloudPropertyRepository
import com.gaatho.rent.features.property.data.repository.LocalPropertyRepository
import com.gaatho.rent.features.property.data.repository.ProxyPropertyRepository

import com.gaatho.rent.features.property.presentation.add.AddPropertyViewModel
import com.gaatho.rent.features.property.presentation.list.PropertyListViewModel
import com.gaatho.rent.features.property.presentation.details.PropertyDetailsViewModel
import com.gaatho.rent.features.property.presentation.edit.EditPropertyViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val propertyModule = module {
    single { LocalPropertyRepository(get()) }
    single { CloudPropertyRepository(get()) }
    single<PropertyRepository> { ProxyPropertyRepository(get(), get(), get()) }

    viewModel { PropertyListViewModel(get(), get(), get(), get(), get()) }
    viewModel { AddPropertyViewModel(get(), get(), get()) }
    viewModel { params ->
        PropertyDetailsViewModel(
            propertyId = params.get(),
            propertyRepository = get(),
            tenantRepository = get(),
            userIdentityProvider = get()
        )
    }
    viewModel { params ->
        EditPropertyViewModel(
            propertyId = params.get(),
            propertyRepository = get(),
            userIdentityProvider = get()
        )
    }
}
