package com.gaatho.rent.features.tenant.presentation.add

import androidx.lifecycle.SavedStateHandle
import com.gaatho.rent.core.auth.UserIdentityProvider
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.core.utils.UuidUtil
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.gaatho.rent.features.tenant.domain.model.Tenant
import com.skydoves.sandwich.ApiResponse
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.catch
import org.orbitmvi.orbit.viewmodel.orbitContainer

class AddTenantViewModel(
    private val tenantRepository: TenantRepository,
    private val propertyRepository: PropertyRepository,
    private val userIdentityProvider: UserIdentityProvider,
    savedStateHandle: SavedStateHandle
) : MviViewModel<AddTenantState, AddTenantSideEffect, AddTenantAction>() {

    private val ownerId: String
        get() = userIdentityProvider.currentUserId()

    override val container = orbitContainer<AddTenantState, AddTenantSideEffect>(
        initialState = AddTenantState(),
        savedStateHandle = savedStateHandle,
        serializer = AddTenantState.serializer()
    ) {
        observeProperties()
    }

    override fun onAction(action: AddTenantAction) {
        when (action) {
            is AddTenantAction.OnFullNameChanged -> intent {
                reduce { state.copy(fullName = action.value) }
            }
            is AddTenantAction.OnPhoneChanged -> intent {
                reduce { state.copy(phone = action.value) }
            }
            is AddTenantAction.OnEmailChanged -> intent {
                reduce { state.copy(email = action.value) }
            }
            is AddTenantAction.OnAddressChanged -> intent {
                reduce { state.copy(address = action.value) }
            }
            is AddTenantAction.OnOccupationChanged -> intent {
                reduce { state.copy(occupation = action.value) }
            }
            is AddTenantAction.OnRoomNumberChanged -> intent {
                reduce { state.copy(roomNumber = action.value) }
            }
            is AddTenantAction.OnDepositChanged -> intent {
                reduce { state.copy(deposit = action.value) }
            }
            is AddTenantAction.OnRentAmountChanged -> intent {
                reduce { state.copy(rentAmount = action.value) }
            }
            is AddTenantAction.OnPropertySelected -> intent {
                reduce { state.copy(selectedPropertyId = action.id) }
            }
            is AddTenantAction.OnDateFieldClicked -> intent {
                reduce { state.copy(showDatePicker = true, isSelectingStartDate = action.isStartDate) }
            }
            is AddTenantAction.OnDateSelected -> intent {
                if (state.isSelectingStartDate) {
                    reduce { state.copy(startDate = action.date, showDatePicker = false) }
                } else {
                    reduce { state.copy(endDate = action.date, showDatePicker = false) }
                }
            }
            is AddTenantAction.OnDatePickerDismissed -> intent {
                reduce { state.copy(showDatePicker = false) }
            }
            is AddTenantAction.OnSaveClicked -> saveTenant()
        }
    }

    private fun observeProperties() = intent(registerIdling = false) {
        propertyRepository.getProperties(ownerId)
            .catch {
                reduce { state.copy(propertiesState = UiState.Error("Failed to load properties")) }
            }
            .collect { properties ->
                val displayModels = properties.map { 
                    PropertySelectionModel(id = it.id, name = it.name)
                }.toImmutableList()
                reduce { state.copy(propertiesState = UiState.Success(displayModels)) }
            }
    }

    private fun saveTenant() = intent {
        if (!state.canSubmit) {
            postSideEffect(AddTenantSideEffect.ShowSnackbar("Please fill all required fields", isError = true))
            return@intent
        }

        reduce { state.copy(isSaving = true) }

        val properties = (state.propertiesState as? UiState.Success)?.data
        val propertyName = properties?.find { it.id == state.selectedPropertyId }?.name
        val now = DateTimeUtil.nowIsoString()

        val tenant = Tenant(
            id = UuidUtil.generateV7String(),
            ownerId = ownerId,
            name = state.fullName.text.trim(),
            email = state.email.text.trim().ifEmpty { null },
            phone = state.phone.text.trim().ifEmpty { null },
            propertyId = state.selectedPropertyId,
            propertyName = propertyName,
            roomNumber = state.roomNumber.text.trim().ifEmpty { null },
            rentAmount = state.rentAmount.text.toLongOrNull() ?: 0L,
            status = "Active",
            createdAt = now,
            updatedAt = now
        )

        when (val response = tenantRepository.createTenant(tenant)) {
            is ApiResponse.Success -> {
                // Reset state to empty but keep properties loaded
                reduce { AddTenantState(propertiesState = state.propertiesState) }
                postSideEffect(AddTenantSideEffect.ShowSnackbar("Tenant added successfully"))
                postSideEffect(AddTenantSideEffect.NavigateBack)
            }
            is ApiResponse.Failure.Error -> {
                reduce { state.copy(isSaving = false) }
                val errorMsg = ErrorMessageExtractor.extract(response, "Failed to add tenant")
                postSideEffect(AddTenantSideEffect.ShowSnackbar(errorMsg, isError = true))
            }
            is ApiResponse.Failure.Exception -> {
                reduce { state.copy(isSaving = false) }
                val errorMsg = ErrorMessageExtractor.extract(response, "Failed to add tenant")
                postSideEffect(AddTenantSideEffect.ShowSnackbar(errorMsg, isError = true))
            }
        }
    }
}
