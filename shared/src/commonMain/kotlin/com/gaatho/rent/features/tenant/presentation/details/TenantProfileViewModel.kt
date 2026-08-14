package com.gaatho.rent.features.tenant.presentation.details

import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.UiState
import kotlinx.coroutines.flow.collectLatest
import org.orbitmvi.orbit.viewmodel.orbitContainer
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.skydoves.sandwich.ApiResponse
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.core.utils.DateTimeUtil

class TenantProfileViewModel(
    private val tenantId: String,
    private val tenantRepository: TenantRepository
) : MviViewModel<TenantProfileState, TenantProfileEffect, TenantProfileAction>() {

    override val container = orbitContainer<TenantProfileState, TenantProfileEffect>(
        initialState = TenantProfileState(tenantId = tenantId)
    ) {
        loadProfile()
    }

    private fun loadProfile() = intent(registerIdling = false) {
        tenantRepository.getTenantById(tenantId).collectLatest { tenant ->
            if (tenant == null) {
                reduce { state.copy(profileState = UiState.Error("Tenant not found")) }
                return@collectLatest
            }
            
            val profile = TenantProfileDisplayModel(
                id = tenant.id,
                name = tenant.name,
                address = tenant.propertyName ?: "Unknown Property",
                isVerified = true,
                avatarUrl = null,
                phone = tenant.phone,
                movedInDate = DateTimeUtil.formatReadableDate(tenant.createdAt)
            )
            
            reduce { state.copy(profileState = UiState.Success(profile)) }
        }
    }

    override fun onAction(action: TenantProfileAction) {
        intent {
            when (action) {
                is TenantProfileAction.OnEditClicked -> {
                    postSideEffect(TenantProfileEffect.NavigateToEdit(tenantId))
                }
                is TenantProfileAction.OnCallClicked -> {
                    val phone = (state.profileState as? UiState.Success)?.data?.phone
                    if (phone != null) {
                        postSideEffect(TenantProfileEffect.OpenPhoneApp(phone))
                    } else {
                        postSideEffect(TenantProfileEffect.ShowError("Phone number not available"))
                    }
                }
                is TenantProfileAction.OnMessageClicked -> {
                    postSideEffect(TenantProfileEffect.ShowToast("Message clicked"))
                }
                is TenantProfileAction.OnEmailClicked -> {
                    postSideEffect(TenantProfileEffect.ShowToast("Email clicked"))
                }
                is TenantProfileAction.OnDeleteClicked -> {
                    reduce { state.copy(showDeleteConfirm = true) }
                }
                is TenantProfileAction.OnDeleteDismissed -> {
                    reduce { state.copy(showDeleteConfirm = false) }
                }
                is TenantProfileAction.OnDeleteConfirmed -> {
                    handleDelete()
                }
            }
        }
    }

    private fun handleDelete() = intent {
        reduce { state.copy(isDeleting = true, showDeleteConfirm = false) }
        when (val result = tenantRepository.deleteTenant(tenantId)) {
            is ApiResponse.Success -> postSideEffect(TenantProfileEffect.NavigateBack)
            is ApiResponse.Failure.Error -> {
                reduce { state.copy(isDeleting = false) }
                postSideEffect(TenantProfileEffect.ShowError(
                    ErrorMessageExtractor.extract(result, "Failed to delete tenant")
                ))
            }
            is ApiResponse.Failure.Exception -> {
                reduce { state.copy(isDeleting = false) }
                postSideEffect(TenantProfileEffect.ShowError(
                    ErrorMessageExtractor.extract(result, "Failed to delete tenant")
                ))
            }
        }
    }
}
