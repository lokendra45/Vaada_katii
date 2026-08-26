package com.gaatho.rent.features.property.presentation.edit

import androidx.compose.ui.text.input.TextFieldValue
import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.property.domain.model.Property
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.flow.firstOrNull
import org.orbitmvi.orbit.viewmodel.orbitContainer

class EditPropertyViewModel(
    private val propertyId: String,
    private val propertyRepository: PropertyRepository,
    private val sessionManager: SessionManager,
) : MviViewModel<EditPropertyState, EditPropertySideEffect, EditPropertyAction>() {

    override val container = orbitContainer<EditPropertyState, EditPropertySideEffect>(
        initialState = EditPropertyState()
    ) {
        loadProperty()
    }

    private fun loadProperty() = intent {
        if (propertyId == "new") {
            reduce { state.copy(isLoading = false) }
            return@intent
        }

        val ownerId = (sessionManager.currentUserId() ?: "")
        val property = propertyRepository
            .getProperties(ownerId)
            .firstOrNull()
            ?.firstOrNull { it.id == propertyId }

        if (property != null) {
            reduce {
                state.copy(
                    isLoading = false,
                    name = TextFieldValue(property.name),
                    streetAddress = TextFieldValue(property.address.split(",").firstOrNull() ?: ""),
                    city = TextFieldValue(property.address.split(",").drop(1).joinToString(",").trim()),
                    propertyType = property.propertyType,
                    totalUnits = TextFieldValue(property.totalUnits.toString()),
                    monthlyRent = TextFieldValue(
                        property.monthlyRent.takeIf { it > 0L }?.toString() ?: ""
                    ),
                    description = TextFieldValue(property.description),
                    billingCycle = "1st of the month",
                    selectedAmenities = setOf("Water", "Electricity")
                )
            }
        } else {
            reduce { state.copy(isLoading = false) }
            postSideEffect(EditPropertySideEffect.ShowSnackbar("Property not found"))
        }
    }

    override fun onAction(action: EditPropertyAction) {
        when (action) {
            is EditPropertyAction.OnNameChanged -> intent {
                reduce { state.copy(name = action.value, nameError = null) }
            }
            is EditPropertyAction.OnStreetAddressChanged -> intent {
                reduce { state.copy(streetAddress = action.value, addressError = null) }
            }
            is EditPropertyAction.OnCityChanged -> intent {
                reduce { state.copy(city = action.value) }
            }
            is EditPropertyAction.OnTypeChanged -> intent {
                reduce { state.copy(propertyType = action.type) }
            }
            is EditPropertyAction.OnTotalUnitsChanged -> intent {
                val digits = action.value.text.filter { it.isDigit() }
                reduce { state.copy(totalUnits = action.value.copy(text = digits)) }
            }
            is EditPropertyAction.OnMonthlyRentChanged -> intent {
                val digits = action.value.text.filter { it.isDigit() }
                reduce { state.copy(monthlyRent = action.value.copy(text = digits)) }
            }
            is EditPropertyAction.OnDescriptionChanged -> intent {
                reduce { state.copy(description = action.value) }
            }
            is EditPropertyAction.OnBillingCycleChanged ->
                intent { reduce { state.copy(billingCycle = action.cycle) } }
            is EditPropertyAction.OnAmenityToggled -> intent {
                val amenities = state.selectedAmenities.toMutableSet()
                if (action.amenity in amenities) amenities.remove(action.amenity)
                else amenities.add(action.amenity)
                reduce { state.copy(selectedAmenities = amenities) }
            }
            is EditPropertyAction.OnSaveClicked -> handleSave()
            is EditPropertyAction.OnSuccessDialogDismissed -> intent {
                reduce { state.copy(showSuccessDialog = false) }
                postSideEffect(EditPropertySideEffect.NavigateBack)
            }
            is EditPropertyAction.OnBackClicked ->
                intent { postSideEffect(EditPropertySideEffect.NavigateBack) }
            is EditPropertyAction.OnDeleteClicked -> intent {
                reduce { state.copy(showDeleteConfirm = true) }
            }
            is EditPropertyAction.OnDeleteDismissed -> intent {
                reduce { state.copy(showDeleteConfirm = false) }
            }
            is EditPropertyAction.OnDeleteConfirmed -> handleDelete()
        }
    }

    private fun handleDelete() = intent {
        reduce { state.copy(showDeleteConfirm = false, isSaving = true) }
        val result = propertyRepository.deleteProperty(propertyId)
        when (result) {
            is ApiResponse.Success -> {
                reduce { state.copy(isSaving = false) }
                postSideEffect(EditPropertySideEffect.NavigateBack)
            }
            is ApiResponse.Failure.Error -> {
                reduce { state.copy(isSaving = false) }
                postSideEffect(EditPropertySideEffect.ShowSnackbar(
                    ErrorMessageExtractor.extract(result, "Failed to delete property")
                ))
            }
            is ApiResponse.Failure.Exception -> {
                reduce { state.copy(isSaving = false) }
                postSideEffect(EditPropertySideEffect.ShowSnackbar(
                    ErrorMessageExtractor.extract(result, "Failed to delete property")
                ))
            }
        }
    }

    private fun handleSave() = intent {
        val s = state
        var hasError = false
        var nameErr: String? = null
        var addressErr: String? = null

        if (s.name.text.isBlank()) {
            nameErr = "Property name is required"
            hasError = true
        }
        if (s.streetAddress.text.isBlank()) {
            addressErr = "Street address is required"
            hasError = true
        }

        if (hasError) {
            reduce { state.copy(nameError = nameErr, addressError = addressErr) }
            return@intent
        }

        reduce { state.copy(isSaving = true) }

        val updated = Property(
            id = if (propertyId == "new") com.gaatho.rent.core.utils.UuidUtil.generateV7String() else propertyId,
            ownerId = (sessionManager.currentUserId() ?: ""),
            name = s.name.text.trim(),
            address = "${s.streetAddress.text.trim()}, ${s.city.text.trim()}",
            propertyType = s.propertyType,
            totalUnits = s.totalUnits.text.toIntOrNull() ?: 1,
            monthlyRent = s.monthlyRent.text.toLongOrNull() ?: 0L,
            description = s.description.text.trim(),
            createdAt = com.gaatho.rent.core.utils.DateTimeUtil.nowIsoString(),
            updatedAt = com.gaatho.rent.core.utils.DateTimeUtil.nowIsoString()
        )

        val result = if (propertyId == "new") {
            propertyRepository.createProperty(updated)
        } else {
            propertyRepository.updateProperty(updated)
        }
        
        when (result) {
            is ApiResponse.Success -> {
                reduce { state.copy(isSaving = false, showSuccessDialog = true) }
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
