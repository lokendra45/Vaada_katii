package com.gaatho.rent.features.property.di

import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.property.data.repository.CloudPropertyRepository
import com.gaatho.rent.features.property.presentation.list.PropertyListViewModel
import com.gaatho.rent.features.property.presentation.details.PropertyDetailsViewModel
import com.gaatho.rent.features.property.presentation.edit.EditPropertyViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val propertyModule = module {
    single<PropertyRepository> { CloudPropertyRepository(get(), get()) }
    viewModel { PropertyListViewModel(get(), get(), get()) }
    viewModel { params ->
        PropertyDetailsViewModel(
            propertyId = params.get(),
            propertyRepository = get(),
            tenantRepository = get(),
            sessionManager = get()
        )
    }
    viewModel { params ->
        com.gaatho.rent.features.property.presentation.details.PropertyIdentityViewModel(
            propertyId = params.get(),
            propertyRepository = get()
        )
    }
    viewModel { params ->
        com.gaatho.rent.features.property.presentation.details.PropertyStatsViewModel(
            propertyId = params.get(),
            tenantRepository = get(),
            sessionManager = get()
        )
    }
    viewModel { params ->
        com.gaatho.rent.features.property.presentation.details.PropertyUnitsViewModel(
            propertyId = params.get(),
            tenantRepository = get(),
            sessionManager = get()
        )
    }
    viewModel { params ->
        EditPropertyViewModel(
            propertyId = params.get(),
            propertyRepository = get(),
            sessionManager = get()
        )
    }
}
