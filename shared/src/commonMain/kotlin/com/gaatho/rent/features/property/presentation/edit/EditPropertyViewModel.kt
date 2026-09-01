package com.gaatho.rent.features.property.presentation.edit

import androidx.compose.ui.text.input.TextFieldValue
import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.network.StorageRepository
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.core.utils.UuidUtil

import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.property.domain.model.Property
import com.gaatho.rent.features.property.domain.model.PropertyUnit
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.flow.firstOrNull
import org.orbitmvi.orbit.viewmodel.orbitContainer

class EditPropertyViewModel(
    private val propertyId: String,
    private val propertyRepository: PropertyRepository,
    private val sessionManager: SessionManager,
    private val storageRepository: StorageRepository
) : MviViewModel<EditPropertyState, EditPropertySideEffect, EditPropertyAction>() {

    override val container = orbitContainer<EditPropertyState, EditPropertySideEffect>(
        initialState = EditPropertyState(isLoading = propertyId != "new")
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
            val unitStates = property.units.map { unit ->
                PropertyUnitState(
                    id = unit.id,
                    name = TextFieldValue(unit.name),
                    monthlyRent = TextFieldValue(unit.monthlyRent.toString())
                )
            }.ifEmpty { 
                List(property.totalUnits) { i -> 
                    PropertyUnitState(name = TextFieldValue("Unit ${i + 1}"), monthlyRent = TextFieldValue(property.monthlyRent.toString())) 
                } 
            }

            reduce {
                state.copy(
                    isLoading = false,
                    name = TextFieldValue(property.name),
                    streetAddress = TextFieldValue(property.address.split(",").firstOrNull() ?: ""),
                    city = TextFieldValue(property.address.split(",").drop(1).joinToString(",").trim()),
                    propertyType = property.propertyType,
                    totalUnits = TextFieldValue(property.totalUnits.toString()),
                    units = unitStates,
                    // monthlyRent removed
                    wifiCharge = TextFieldValue(property.wifiCharge.toString()),
                    waterCharge = TextFieldValue(property.waterCharge.toString()),
                    electricityCharge = TextFieldValue(property.electricityCharge.toString()),
                    wasteCharge = TextFieldValue(property.wasteCharge.toString()),
                    imageUrl = property.imageUrl,
                    uploadedImageName = property.imageUrl?.substringAfterLast("/"),
                    description = TextFieldValue(property.description),
                    billingCycle = property.billingCycle,
                    selectedAmenities = property.amenities,
                    originalCreatedAt = property.createdAt
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
                val count = digits.toIntOrNull() ?: 1
                
                val newUnits = List(count) { i -> 
                    state.units.getOrNull(i) ?: PropertyUnitState(
                        id = UuidUtil.generateV7String(),
                        name = TextFieldValue("Unit ${i + 1}"),
                        monthlyRent = TextFieldValue("")
                    )
                }
                reduce { state.copy(totalUnits = action.value.copy(text = digits), units = newUnits) }
            }
            is EditPropertyAction.OnUnitNameChanged -> intent {
                val newUnits = state.units.toMutableList()
                if (action.index in newUnits.indices) {
                    newUnits[action.index] = newUnits[action.index].copy(name = action.name)
                    reduce { state.copy(units = newUnits) }
                }
            }
            is EditPropertyAction.OnUnitRentChanged -> intent {
                val newUnits = state.units.toMutableList()
                if (action.index in newUnits.indices) {
                    val digits = action.rent.text.filter { it.isDigit() }
                    newUnits[action.index] = newUnits[action.index].copy(monthlyRent = action.rent.copy(text = digits))
                    reduce { state.copy(units = newUnits) }
                }
            }
            is EditPropertyAction.OnWifiChargeChanged -> intent {
                val digits = action.value.text.filter { it.isDigit() }
                reduce { state.copy(wifiCharge = action.value.copy(text = digits)) }
            }
            is EditPropertyAction.OnWaterChargeChanged -> intent {
                val digits = action.value.text.filter { it.isDigit() }
                reduce { state.copy(waterCharge = action.value.copy(text = digits)) }
            }
            is EditPropertyAction.OnElectricityChargeChanged -> intent {
                val digits = action.value.text.filter { it.isDigit() }
                reduce { state.copy(electricityCharge = action.value.copy(text = digits)) }
            }
            is EditPropertyAction.OnWasteChargeChanged -> intent {
                val digits = action.value.text.filter { it.isDigit() }
                reduce { state.copy(wasteCharge = action.value.copy(text = digits)) }
            }
            is EditPropertyAction.OnImagePicked -> intent {
                reduce { state.copy(isCompressingImage = true) }
                // Just store the bytes locally – we upload when the user hits Save. It's already compressed by AppImagePicker.
                reduce {
                    state.copy(
                        isCompressingImage = false,
                        pendingImageBytes = action.bytes,
                        pendingImageName = action.name,
                        uploadedImageName = action.name   // show filename in UI immediately
                    )
                }
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
            is EditPropertyAction.OnAddCustomAmenity -> intent {
                if (action.amenity.isNotBlank()) {
                    val amenities = state.selectedAmenities.toMutableSet()
                    amenities.add(action.amenity.trim())
                    reduce { state.copy(selectedAmenities = amenities) }
                }
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
                postSideEffect(EditPropertySideEffect.NavigateToPropertyList)
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
        if (s.isSaving) return@intent
        
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

        // Upload image now if one was staged
        val pendingBytes = s.pendingImageBytes
        val finalImageUrl: String? = if (pendingBytes != null) {
            val path = "${UuidUtil.generateV7String()}_${s.pendingImageName ?: "image.jpg"}"
            when (val r = storageRepository.uploadFile("properties", path, pendingBytes)) {
                is ApiResponse.Success -> r.data
                is ApiResponse.Failure.Error -> {
                    reduce { state.copy(isSaving = false) }
                    postSideEffect(EditPropertySideEffect.ShowSnackbar("Failed to upload image"))
                    return@intent
                }
                is ApiResponse.Failure.Exception -> {
                    reduce { state.copy(isSaving = false) }
                    postSideEffect(EditPropertySideEffect.ShowSnackbar("Failed to upload image"))
                    return@intent
                }
            }
        } else s.imageUrl

        val domainUnits = s.units.map { u ->
            PropertyUnit(
                id = u.id.ifBlank { UuidUtil.generateV7String() },
                name = u.name.text,
                monthlyRent = u.monthlyRent.text.toLongOrNull() ?: 0L
            )
        }

        val updated = Property(
            id = if (propertyId == "new") UuidUtil.generateV7String() else propertyId,
            ownerId = (sessionManager.currentUserId() ?: ""),
            name = s.name.text.trim(),
            address = "${s.streetAddress.text.trim()}, ${s.city.text.trim()}",
            propertyType = s.propertyType,
            totalUnits = s.totalUnits.text.toIntOrNull() ?: 1,
            units = domainUnits,
            imageUrl = finalImageUrl,
            monthlyRent = domainUnits.firstOrNull()?.monthlyRent ?: 0L, // Use first unit's rent or 0 as a fallback
            wifiCharge = s.wifiCharge.text.toLongOrNull() ?: 0L,
            waterCharge = s.waterCharge.text.toLongOrNull() ?: 0L,
            electricityCharge = s.electricityCharge.text.toLongOrNull() ?: 0L,
            wasteCharge = s.wasteCharge.text.toLongOrNull() ?: 0L,
            description = s.description.text.trim(),
            billingCycle = s.billingCycle,
            amenities = s.selectedAmenities,
            createdAt = if (propertyId == "new") DateTimeUtil.nowIsoString() else s.originalCreatedAt,
            updatedAt = DateTimeUtil.nowIsoString()
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
