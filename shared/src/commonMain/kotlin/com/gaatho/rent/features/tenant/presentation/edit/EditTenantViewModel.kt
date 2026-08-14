package com.gaatho.rent.features.tenant.presentation.edit

import com.gaatho.rent.core.mvi.MviViewModel
import org.orbitmvi.orbit.viewmodel.orbitContainer
import androidx.compose.ui.text.input.TextFieldValue

import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.core.auth.UserIdentityProvider
import com.gaatho.rent.features.tenant.domain.model.Tenant
import com.gaatho.rent.core.utils.UuidUtil
import com.gaatho.rent.core.utils.DateTimeUtil
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.flow.firstOrNull

class EditTenantViewModel(
    private val tenantId: String,
    private val tenantRepository: TenantRepository,
    private val propertyRepository: PropertyRepository,
    private val userIdentityProvider: UserIdentityProvider
) : MviViewModel<EditTenantState, EditTenantSideEffect, EditTenantAction>() {

    private val ownerId: String
        get() = userIdentityProvider.currentUserId()

    override val container = orbitContainer<EditTenantState, EditTenantSideEffect>(EditTenantState()) {
        loadTenant()
    }

    private fun loadTenant() = intent {
        reduce { state.copy(isLoading = true) }

        val properties = propertyRepository.getProperties(ownerId).firstOrNull() ?: emptyList()
        val propertyOptions = properties.map { PropertyOption(it.id, it.name) }

        if (tenantId == "new") {
            reduce {
                state.copy(
                    isLoading = false,
                    propertyOptions = propertyOptions
                )
            }
        } else {
            val tenant = tenantRepository.getTenantById(tenantId).firstOrNull()
            if (tenant != null) {
                reduce {
                    state.copy(
                        isLoading = false,
                        name = TextFieldValue(tenant.name),
                        phone = TextFieldValue(tenant.phone ?: ""),
                        email = TextFieldValue(tenant.email ?: ""),
                        rentAmount = TextFieldValue(tenant.rentAmount.toString()),
                        propertyId = tenant.propertyId ?: "",
                        unitNumber = TextFieldValue(tenant.roomNumber ?: ""),
                        status = tenant.status,
                        propertyOptions = propertyOptions
                    )
                }
            } else {
                reduce { state.copy(isLoading = false, propertyOptions = propertyOptions) }
            }
        }
    }

    override fun onAction(action: EditTenantAction) {
        when (action) {
            is EditTenantAction.OnNameChanged -> intent {
                reduce { state.copy(name = action.value, nameError = null) }
            }
            is EditTenantAction.OnPhoneChanged -> intent {
                reduce { state.copy(phone = action.value) }
            }
            is EditTenantAction.OnEmailChanged -> intent {
                reduce { state.copy(email = action.value) }
            }
            is EditTenantAction.OnRentChanged -> intent {
                val digits = action.value.text.filter { it.isDigit() }
                reduce { state.copy(rentAmount = action.value.copy(text = digits), rentError = null) }
            }
            is EditTenantAction.OnUnitNumberChanged -> intent {
                reduce { state.copy(unitNumber = action.value) }
            }
            is EditTenantAction.OnMoveInDateChanged -> intent {
                reduce { state.copy(moveInDate = action.date) }
            }
            is EditTenantAction.OnLeaseDurationSelected -> intent {
                reduce { state.copy(leaseDuration = action.duration) }
            }
            is EditTenantAction.OnSecurityDepositChanged -> intent {
                val digits = action.value.text.filter { it.isDigit() }
                reduce { state.copy(securityDeposit = action.value.copy(text = digits)) }
            }
            is EditTenantAction.OnPropertySelected -> intent {
                reduce { state.copy(propertyId = action.propertyId) }
            }
            is EditTenantAction.OnStatusSelected -> intent {
                reduce { state.copy(status = action.status) }
            }
            is EditTenantAction.OnSaveClicked -> saveTenant()
            is EditTenantAction.OnSuccessDialogDismissed -> intent {
                reduce { state.copy(showSuccessDialog = false) }
                postSideEffect(EditTenantSideEffect.NavigateBack)
            }
            is EditTenantAction.OnBackClicked -> intent {
                postSideEffect(EditTenantSideEffect.NavigateBack)
            }
            is EditTenantAction.OnDeleteClicked -> intent {
                reduce { state.copy(showDeleteConfirm = true) }
            }
            is EditTenantAction.OnDeleteDismissed -> intent {
                reduce { state.copy(showDeleteConfirm = false) }
            }
            is EditTenantAction.OnDeleteConfirmed -> deleteTenant()
        }
    }

    private fun deleteTenant() = intent {
        reduce { state.copy(showDeleteConfirm = false, isSaving = true) }
        when (val response = tenantRepository.deleteTenant(tenantId)) {
            is ApiResponse.Success -> {
                reduce { state.copy(isSaving = false) }
                postSideEffect(EditTenantSideEffect.NavigateBack)
            }
            is ApiResponse.Failure.Error, is ApiResponse.Failure.Exception -> {
                reduce { state.copy(isSaving = false) }
                postSideEffect(EditTenantSideEffect.ShowSnackbar("Failed to remove tenant"))
            }
        }
    }

    private fun saveTenant() = intent {
        val currentState = state
        var hasError = false
        var nameErr: String? = null
        var rentErr: String? = null

        if (currentState.name.text.isBlank()) {
            nameErr = "Name cannot be empty"
            hasError = true
        }
        if (currentState.rentAmount.text.isBlank()) {
            rentErr = "Rent cannot be empty"
            hasError = true
        }

        if (hasError) {
            reduce { state.copy(nameError = nameErr, rentError = rentErr) }
            return@intent
        }

        reduce { state.copy(isSaving = true) }

        val propertyName = currentState.propertyOptions.find { it.id == currentState.propertyId }?.name ?: ""

        val tenantToSave = Tenant(
            id = if (tenantId == "new") UuidUtil.generateV7String() else tenantId,
            ownerId = ownerId,
            name = currentState.name.text,
            email = currentState.email.text.takeIf { it.isNotBlank() },
            phone = currentState.phone.text.takeIf { it.isNotBlank() },
            propertyId = currentState.propertyId?.takeIf { it.isNotBlank() },
            propertyName = propertyName.takeIf { it.isNotBlank() },
            roomNumber = currentState.unitNumber.text.takeIf { it.isNotBlank() },
            rentAmount = currentState.rentAmount.text.toLongOrNull() ?: 0L,
            status = currentState.status,
            createdAt = DateTimeUtil.nowIsoString(),
            updatedAt = DateTimeUtil.nowIsoString()
        )

        val response = if (tenantId == "new") {
            tenantRepository.createTenant(tenantToSave)
        } else {
            tenantRepository.updateTenant(tenantToSave)
        }

        reduce { state.copy(isSaving = false) }

        when (response) {
            is ApiResponse.Success -> {
                reduce { state.copy(showSuccessDialog = true) }
            }
            is ApiResponse.Failure.Error, is ApiResponse.Failure.Exception -> {
                postSideEffect(EditTenantSideEffect.ShowSnackbar("Failed to save tenant"))
            }
        }
    }
}