package com.gaatho.rent.features.property.presentation.details

import com.gaatho.rent.core.auth.UserIdentityProvider
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.gaatho.rent.features.tenant.domain.model.Tenant
import com.skydoves.sandwich.ApiResponse
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.orbitmvi.orbit.viewmodel.orbitContainer

class PropertyDetailsViewModel(
    private val propertyId: String,
    private val propertyRepository: PropertyRepository,
    private val tenantRepository: TenantRepository,
    private val userIdentityProvider: UserIdentityProvider,
) : MviViewModel<PropertyDetailsState, PropertyDetailsSideEffect, PropertyDetailsAction>() {

    override val container = orbitContainer<PropertyDetailsState, PropertyDetailsSideEffect>(
        initialState = PropertyDetailsState()
    ) {
        observeData()
    }

    private fun observeData() = intent {
        // Top level viewmodel doesn't load component state anymore
    }


    override fun onAction(action: PropertyDetailsAction) {
        when (action) {
            PropertyDetailsAction.OnBackClicked ->
                intent { postSideEffect(PropertyDetailsSideEffect.NavigateBack) }

            PropertyDetailsAction.OnEditClicked ->
                intent { postSideEffect(PropertyDetailsSideEffect.NavigateToEdit(propertyId)) }

            PropertyDetailsAction.OnAddTenantClicked ->
                intent { postSideEffect(PropertyDetailsSideEffect.NavigateToAddTenant) }

            PropertyDetailsAction.OnDeleteClicked ->
                intent { reduce { state.copy(showDeleteConfirm = true) } }

            PropertyDetailsAction.OnDeleteDismissed ->
                intent { reduce { state.copy(showDeleteConfirm = false) } }

            PropertyDetailsAction.OnDeleteConfirmed -> handleDelete()

            is PropertyDetailsAction.OnUnitClicked -> { /* navigate to unit detail */ }

            PropertyDetailsAction.OnViewAllUnitsClicked -> { /* navigate to unit list */ }
        }
    }

    private fun handleDelete() = intent {
        reduce { state.copy(isDeleting = true, showDeleteConfirm = false) }
        when (val result = propertyRepository.deleteProperty(propertyId)) {
            is ApiResponse.Success -> postSideEffect(PropertyDetailsSideEffect.NavigateBack)
            is ApiResponse.Failure.Error -> {
                reduce { state.copy(isDeleting = false) }
                postSideEffect(PropertyDetailsSideEffect.ShowError(
                    ErrorMessageExtractor.extract(result, "Failed to delete property")
                ))
            }
            is ApiResponse.Failure.Exception -> {
                reduce { state.copy(isDeleting = false) }
                postSideEffect(PropertyDetailsSideEffect.ShowError(
                    ErrorMessageExtractor.extract(result, "Failed to delete property")
                ))
            }
        }
    }
}
