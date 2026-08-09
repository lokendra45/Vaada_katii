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
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.compressImage
import io.github.vinceglb.filekit.ImageFormat

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
                val error = if (action.value.text.isBlank()) "Property name is required" else null
                reduce { state.copy(name = action.value, nameError = error) }
            }
            is AddPropertyAction.OnStreetAddressChanged -> intent {
                val error = if (action.value.text.isBlank()) "Street address is required" else null
                reduce { state.copy(streetAddress = action.value, addressError = error) }
            }
            is AddPropertyAction.OnCityChanged -> intent {
                val error = if (action.value.text.isBlank()) "City is required" else null
                reduce { state.copy(city = action.value, cityError = error) }
            }
            is AddPropertyAction.OnTypeChanged -> intent {
                reduce { state.copy(propertyType = action.type) }
            }
            is AddPropertyAction.OnTotalUnitsChanged -> intent {
                val isNumber = action.value.text.toIntOrNull() != null
                val error = if (action.value.text.isBlank()) "Units is required" else if (!isNumber) "Invalid number" else null
                reduce { state.copy(totalUnits = action.value, unitsError = error) }
            }
            is AddPropertyAction.OnBillingCycleChanged -> intent {
                reduce { state.copy(billingCycle = action.cycle) }
            }
            is AddPropertyAction.OnAmenityToggled -> intent {
                val amenities = state.selectedAmenities.toMutableSet()
                if (amenities.contains(action.amenity)) amenities.remove(action.amenity)
                else amenities.add(action.amenity)
                reduce { state.copy(selectedAmenities = amenities) }
            }
            is AddPropertyAction.OnImagePicked -> intent {
                reduce { state.copy(imageBytes = action.bytes) }
            }
            is AddPropertyAction.OnSaveClicked -> handleSave()
        }
    }

    @OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)
    private fun handleSave() = intent {
        val currentState = state
        var hasError = false

        if (currentState.name.text.isBlank()) {
            reduce { state.copy(nameError = "Property name is required") }
            hasError = true
        }
        
        if (currentState.streetAddress.text.isBlank()) {
            reduce { state.copy(addressError = "Street address is required") }
            hasError = true
        }
        
        if (currentState.city.text.isBlank()) {
            reduce { state.copy(cityError = "City is required") }
            hasError = true
        }
        
        if (currentState.totalUnits.text.toIntOrNull() == null) {
            reduce { state.copy(unitsError = "Invalid number of units") }
            hasError = true
        }

        if (hasError) return@intent

        reduce { state.copy(isSaving = true) }

        val base64Image = currentState.imageBytes?.let { bytes ->
            val compressedBytes = FileKit.compressImage(
                bytes = bytes,
                quality = 80,
                maxWidth = 1024,
                maxHeight = 1024,
                imageFormat = ImageFormat.JPEG
            )
            "base64:" + kotlin.io.encoding.Base64.Default.encode(compressedBytes)
        }

        val property = Property(
            id = UuidUtil.generateV7String(), 
            ownerId = ownerId,
            name = currentState.name.text.trim(),
            address = "${currentState.streetAddress.text.trim()}, ${currentState.city.text.trim()}".trim(',' , ' '),
            propertyType = currentState.propertyType,
            totalUnits = currentState.totalUnits.text.toIntOrNull() ?: 1,
            billingCycle = currentState.billingCycle,
            amenities = currentState.selectedAmenities,
            imageUrl = base64Image
        )

        when (val response = repository.createProperty(property)) {
            is ApiResponse.Success -> {
                postSideEffect(AddPropertySideEffect.ShowSuccessDialog)
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
