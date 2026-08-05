package com.gaatho.rent.features.property.presentation.edit

import com.gaatho.rent.core.auth.UserIdentityProvider
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.property.domain.model.Property
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.flow.first
import org.orbitmvi.orbit.viewmodel.orbitContainer

class EditPropertyViewModel(
    private val propertyId: String,
    private val propertyRepository: PropertyRepository,
    private val userIdentityProvider: UserIdentityProvider,
) : MviViewModel<EditPropertyState, EditPropertySideEffect, EditPropertyAction>() {

    override val container = orbitContainer<EditPropertyState, EditPropertySideEffect>(
        initialState = EditPropertyState()
    ) {
        loadProperty()
    }

    private fun loadProperty() = intent {
        val ownerId = userIdentityProvider.currentUserId()
        val property = propertyRepository
            .getProperties(ownerId)
            .first()
            .firstOrNull { it.id == propertyId }

        if (property != null) {
            // Split combined address back into street + city parts for the form
            val parts = property.address.split(",").map { it.trim() }
            val street = parts.getOrElse(0) { "" }
            val city = parts.drop(1).joinToString(", ").trim()

            reduce {
                state.copy(
                    name = property.name,
                    streetAddress = street,
                    city = city,
                    propertyType = property.propertyType,
                    isLoading = false
                )
            }
        } else {
            reduce { state.copy(isLoading = false) }
            postSideEffect(EditPropertySideEffect.ShowSnackbar("Property not found"))
        }
    }

    override fun onAction(action: EditPropertyAction) {
        when (action) {
            is EditPropertyAction.OnNameChanged ->
                intent { reduce { state.copy(name = action.name, nameError = null) } }
            is EditPropertyAction.OnStreetAddressChanged ->
                intent { reduce { state.copy(streetAddress = action.address, addressError = null) } }
            is EditPropertyAction.OnCityChanged ->
                intent { reduce { state.copy(city = action.city) } }
            is EditPropertyAction.OnTypeChanged ->
                intent { reduce { state.copy(propertyType = action.type) } }
            is EditPropertyAction.OnTotalUnitsChanged ->
                intent { reduce { state.copy(totalUnits = action.units) } }
            is EditPropertyAction.OnBillingCycleChanged ->
                intent { reduce { state.copy(billingCycle = action.cycle) } }
            is EditPropertyAction.OnAmenityToggled -> intent {
                val amenities = state.selectedAmenities.toMutableSet()
                if (action.amenity in amenities) amenities.remove(action.amenity)
                else amenities.add(action.amenity)
                reduce { state.copy(selectedAmenities = amenities) }
            }
            is EditPropertyAction.OnSaveClicked -> handleSave()
            is EditPropertyAction.OnBackClicked ->
                intent { postSideEffect(EditPropertySideEffect.NavigateBack) }
        }
    }

    private fun handleSave() = intent {
        val s = state
        var hasError = false

        if (s.name.isBlank()) {
            reduce { state.copy(nameError = "Property name is required") }
            hasError = true
        }
        if (s.streetAddress.isBlank()) {
            reduce { state.copy(addressError = "Street address is required") }
            hasError = true
        }
        if (hasError) return@intent

        reduce { state.copy(isSaving = true) }

        val updated = Property(
            id = propertyId,
            ownerId = userIdentityProvider.currentUserId(),
            name = s.name.trim(),
            address = buildString {
                append(s.streetAddress.trim())
                if (s.city.isNotBlank()) append(", ${s.city.trim()}")
            },
            propertyType = s.propertyType
        )

        when (val result = propertyRepository.updateProperty(updated)) {
            is ApiResponse.Success -> {
                postSideEffect(EditPropertySideEffect.NavigateBack)
            }
            is ApiResponse.Failure.Error -> {
                reduce { state.copy(isSaving = false) }
                postSideEffect(EditPropertySideEffect.ShowSnackbar(
                    ErrorMessageExtractor.extract(result, "Failed to update property")
                ))
            }
            is ApiResponse.Failure.Exception -> {
                reduce { state.copy(isSaving = false) }
                postSideEffect(EditPropertySideEffect.ShowSnackbar(
                    ErrorMessageExtractor.extract(result, "Failed to update property")
                ))
            }
        }
    }
}
