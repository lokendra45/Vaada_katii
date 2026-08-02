package com.gaatho.rent.features.property.presentation.add

import androidx.lifecycle.SavedStateHandle
import com.gaatho.rent.core.auth.UserIdentityProvider
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.property.domain.model.Property
import com.skydoves.sandwich.onFailure
import com.skydoves.sandwich.onSuccess
import org.orbitmvi.orbit.viewmodel.orbitContainer
import com.skydoves.sandwich.ApiResponse
import com.gaatho.rent.core.utils.UuidUtil

class AddPropertyViewModel(
    private val repository: PropertyRepository,
    private val userIdentityProvider: UserIdentityProvider,
    savedStateHandle: SavedStateHandle
) : MviViewModel<AddPropertyState, AddPropertySideEffect, AddPropertyAction>() {

    /**
     * After [SqlDelightGuestSessionManager] caches the value on first access,
     * this property is a pure in-memory lookup — no DB hit, safe on any thread.
     */
    private val ownerId: String
        get() = userIdentityProvider.currentUserId()

    override val container = orbitContainer<AddPropertyState, AddPropertySideEffect>(
        initialState = AddPropertyState(),
        savedStateHandle = savedStateHandle,
        serializer = AddPropertyState.serializer()
    )

    override fun onAction(action: AddPropertyAction) {
        when (action) {
            is AddPropertyAction.OnNameChanged -> intent {
                reduce { state.copy(name = action.name, nameError = null) }
            }
            is AddPropertyAction.OnStreetAddressChanged -> intent {
                reduce { state.copy(streetAddress = action.address, addressError = null) }
            }
            is AddPropertyAction.OnCityChanged -> intent {
                reduce { state.copy(city = action.city) }
            }
            is AddPropertyAction.OnZipCodeChanged -> intent {
                reduce { state.copy(zipCode = action.zip) }
            }
            is AddPropertyAction.OnTypeChanged -> intent {
                reduce { state.copy(propertyType = action.type) }
            }
            is AddPropertyAction.OnTotalUnitsChanged -> intent {
                reduce { state.copy(totalUnits = action.units) }
            }
            is AddPropertyAction.OnBillingCycleChanged -> intent {
                reduce { state.copy(billingCycle = action.cycle) }
            }
            is AddPropertyAction.OnAmenityToggled -> intent {
                val amenities = state.selectedAmenities.toMutableSet()
                if (amenities.contains(action.amenity)) {
                    amenities.remove(action.amenity)
                } else {
                    amenities.add(action.amenity)
                }
                reduce { state.copy(selectedAmenities = amenities) }
            }
            is AddPropertyAction.OnSaveClicked -> handleSave()
        }
    }

    private fun handleSave() = intent {
        val currentState = state
        var hasError = false

        if (currentState.name.isBlank()) {
            reduce { state.copy(nameError = "Property name is required") }
            hasError = true
        }
        
        if (currentState.streetAddress.isBlank()) {
            reduce { state.copy(addressError = "Street address is required") }
            hasError = true
        }

        if (hasError) return@intent

        reduce { state.copy(isSaving = true) }

        val property = Property(
            id = UuidUtil.generateV7String(), 
            ownerId = ownerId,
            name = currentState.name.trim(),
            address = "${currentState.streetAddress.trim()}, ${currentState.city.trim()} ${currentState.zipCode.trim()}".trim(',' , ' '),
            propertyType = currentState.propertyType
        )

        when (val response = repository.createProperty(property)) {
            is ApiResponse.Success -> {
                postSideEffect(AddPropertySideEffect.NavigateBack)
            }
            is ApiResponse.Failure.Error -> {
                reduce { state.copy(isSaving = false) }
                val errorMsg = ErrorMessageExtractor.extract(response, "Failed to save property. You might be offline.")
                postSideEffect(AddPropertySideEffect.ShowSnackbar(errorMsg))
            }
            is ApiResponse.Failure.Exception -> {
                reduce { state.copy(isSaving = false) }
                val errorMsg = ErrorMessageExtractor.extract(response, "Failed to save property. You might be offline.")
                postSideEffect(AddPropertySideEffect.ShowSnackbar(errorMsg))
            }
        }
    }
}
