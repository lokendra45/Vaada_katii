package com.gaatho.rent.features.tenant.presentation.edit

import com.gaatho.rent.core.mvi.MviViewModel
import kotlinx.coroutines.delay
import org.orbitmvi.orbit.viewmodel.orbitContainer
import kotlin.time.Duration.Companion.milliseconds

class EditTenantViewModel(
    private val tenantId: String
) : MviViewModel<EditTenantState, EditTenantSideEffect, EditTenantAction>() {

    override val container = orbitContainer<EditTenantState, EditTenantSideEffect>(EditTenantState()) {
        loadTenant()
    }

    private fun loadTenant() = intent {
        // Simulate loading from repository
        reduce { state.copy(isLoading = true) }
        delay(800.milliseconds) // Mock network delay
        
        // Mock data
        val mockProperties = listOf(
            PropertyOption("prop-1", "Sunrise Residency"),
            PropertyOption("prop-2", "Green Valley Flats")
        )
        
        if (tenantId == "new") {
            reduce { 
                state.copy(
                    isLoading = false,
                    propertyOptions = mockProperties
                ) 
            }
        } else {
            reduce {
                state.copy(
                    isLoading = false,
                    name = "Suman Shrestha",
                    phone = "+977-9841234567",
                    email = "suman@example.com",
                    rentAmount = "15000",
                    propertyId = "prop-1",
                    roomNumber = "2A",
                    status = "Active",
                    propertyOptions = mockProperties
                )
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
        delay(1000) // Mock network delay
        reduce { state.copy(isSaving = false) }
        postSideEffect(EditTenantSideEffect.ShowSnackbar("Tenant saved successfully"))
        postSideEffect(EditTenantSideEffect.NavigateBack)
    }
}
