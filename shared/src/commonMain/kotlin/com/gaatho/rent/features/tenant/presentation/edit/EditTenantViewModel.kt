package com.gaatho.rent.features.tenant.presentation.edit

import com.gaatho.rent.core.mvi.MviViewModel
import kotlinx.coroutines.delay
import org.orbitmvi.orbit.viewmodel.orbitContainer
import kotlin.time.Duration.Companion.milliseconds

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
                        name = tenant.name,
                        phone = tenant.phone ?: "",
                        email = tenant.email ?: "",
                        rentAmount = tenant.rentAmount.toString(),
                        propertyId = tenant.propertyId ?: "",
                        roomNumber = tenant.roomNumber ?: "",
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
                reduce { state.copy(name = action.name, nameError = null) }
            }
            is EditTenantAction.OnPhoneChanged -> intent {
                reduce { state.copy(phone = action.phone) }
            }
            is EditTenantAction.OnEmailChanged -> intent {
                reduce { state.copy(email = action.email) }
            }
            is EditTenantAction.OnRentChanged -> intent {
                val digits = action.rent.filter { it.isDigit() }
                reduce { state.copy(rentAmount = digits, rentError = null) }
            }
            is EditTenantAction.OnRoomNumberChanged -> intent {
                reduce { state.copy(roomNumber = action.roomNumber) }
            }
            is EditTenantAction.OnPropertySelected -> intent {
                reduce { state.copy(propertyId = action.propertyId) }
            }
            is EditTenantAction.OnStatusSelected -> intent {
                reduce { state.copy(status = action.status) }
            }
            is EditTenantAction.OnSaveClicked -> saveTenant()
            is EditTenantAction.OnBackClicked -> intent {
                postSideEffect(EditTenantSideEffect.NavigateBack)
            }
        }
    }

    private fun saveTenant() = intent {
        val currentState = state
        var hasError = false
        var nameErr: String? = null
        var rentErr: String? = null
        
        if (currentState.name.isBlank()) {
            nameErr = "Name cannot be empty"
            hasError = true
        }
        if (currentState.rentAmount.isBlank()) {
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
            name = currentState.name,
            email = currentState.email.takeIf { it.isNotBlank() },
            phone = currentState.phone.takeIf { it.isNotBlank() },
            propertyId = currentState.propertyId?.takeIf { it.isNotBlank() },
            propertyName = propertyName.takeIf { it.isNotBlank() },
            roomNumber = currentState.roomNumber.takeIf { it.isNotBlank() },
            rentAmount = currentState.rentAmount.toLongOrNull() ?: 0L,
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
                postSideEffect(EditTenantSideEffect.ShowSnackbar("Tenant saved successfully"))
                postSideEffect(EditTenantSideEffect.NavigateBack)
            }
            is ApiResponse.Failure.Error, is ApiResponse.Failure.Exception -> {
                postSideEffect(EditTenantSideEffect.ShowSnackbar("Failed to save tenant"))
            }
        }
    }
}
