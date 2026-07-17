package com.gaatho.rent.features.property.di

import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.property.data.repository.CloudPropertyRepository
import com.gaatho.rent.features.property.data.repository.LocalPropertyRepository
import com.gaatho.rent.features.property.data.repository.ProxyPropertyRepository

import com.gaatho.rent.features.property.presentation.add.AddPropertyViewModel
import com.gaatho.rent.features.property.presentation.list.PropertyListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val propertyModule = module {
    single { LocalPropertyRepository(get()) }          // RentManagerDatabase
    single { CloudPropertyRepository(get()) }          // SupabaseClient
    
    // Bind Proxy to the main Interface
    single<PropertyRepository> { ProxyPropertyRepository(get(), get(), get()) } // Local, Cloud, Paywall
    
    viewModel { PropertyListViewModel(get(), get(), get()) } // PropertyRepository, SupabaseClient, SavedStateHandle
    viewModel { AddPropertyViewModel(get(), get(), get()) } // PropertyRepository, SupabaseClient, SavedStateHandle
}
