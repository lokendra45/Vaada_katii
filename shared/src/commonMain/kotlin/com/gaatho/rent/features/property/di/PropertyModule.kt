package com.gaatho.rent.features.property.di

import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.property.data.repository.PropertyStore
import com.gaatho.rent.features.property.presentation.add.AddPropertyViewModel
import com.gaatho.rent.features.property.presentation.list.PropertyListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for the Property feature.
 *
 * All Store, Repository, and ViewModel registrations for this feature are here.
 *
 * ## SavedStateHandle injection
 * Using Koin's `viewModel { }` DSL with `get()` for [PropertyListViewModel] correctly
 * injects `SavedStateHandle` on both Android and iOS (via the JetBrains multiplatform
 * lifecycle library). Koin's ViewModel factory automatically resolves it.
 *
 * ## Fix applied
 * [PropertyStore] constructor correctly uses 2 parameters — not 3.
 */
val propertyModule = module {
    single { PropertyStore(get(), get()) }       // SupabaseClient, RentManagerDatabase
    single { PropertyRepository(get()) }          // PropertyStore
    viewModel { PropertyListViewModel(get(), get(), get()) } // PropertyRepository, SupabaseClient, SavedStateHandle
    viewModel { AddPropertyViewModel(get(), get(), get()) } // PropertyRepository, SupabaseClient, SavedStateHandle
}
