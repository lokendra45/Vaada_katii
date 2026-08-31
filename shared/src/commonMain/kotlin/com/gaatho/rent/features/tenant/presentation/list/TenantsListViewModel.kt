package com.gaatho.rent.features.tenant.presentation.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.utils.TenantUtils
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.tenant.domain.model.Tenant
import com.gaatho.rent.features.tenant.domain.usecase.GetArchivedTenantsUseCase
import com.gaatho.rent.features.tenant.domain.usecase.GetPagedTenantsUseCase
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.viewmodel.orbitContainer

class TenantsListViewModel(
    private val getPagedTenants: GetPagedTenantsUseCase,
    private val propertyRepository: PropertyRepository,
    private val sessionManager: SessionManager,
    private val getArchivedTenants: GetArchivedTenantsUseCase,
    private val deleteTenant: com.gaatho.rent.features.tenant.domain.usecase.DeleteTenantUseCase,
    savedStateHandle: SavedStateHandle
) : MviViewModel<TenantsListState, TenantsListSideEffect, TenantsListAction>() {

    private val ownerId: String
        get() = (sessionManager.currentUserId() ?: "")

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchText.value = query
    }

    override val container = orbitContainer<TenantsListState, TenantsListSideEffect>(
        initialState = TenantsListState(),
        savedStateHandle = savedStateHandle,
        serializer = TenantsListState.serializer()
    ) {
        observeProperties()
        checkArchivedTenants()
    }

    @OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    val pagedTenantsFlow: Flow<PagingData<TenantDisplayModel>> = combine(
        _searchText
            .debounce(500L)
            .onEach { _isSearching.value = true }
            .distinctUntilChanged(),
        // Filters (status, property) still live in Orbit state — intentional taps, not rapid typing.
        container.stateFlow
            .map { state ->
                FilterParams(
                    status = state.selectedStatus,
                    propertyName = state.selectedProperty,
                    properties = (state.propertiesState as? UiState.Success)?.data
                )
            }
            .distinctUntilChanged()
    ) { search, filters ->
        search to filters
    }
        .flatMapLatest { pair ->
            val search = pair.first
            val filters = pair.second

            getPagedTenants(
                ownerId = ownerId,
                searchQuery = search,
                statusFilter = filters.status,
                propertyFilter = filters.propertyName,
                properties = filters.properties
            ).map { pagingData ->
                _isSearching.value = false
                pagingData.map { mapToDisplayModel(it) }
            }.catch { e ->
                _isSearching.value = false
                AppLogger.network.e(e) { "Tenants paging flow failed" }
                emit(PagingData.empty())
            }
        }
        .cachedIn(viewModelScope)

    private data class FilterParams(
        val status: String,
        val propertyName: String,
        val properties: List<com.gaatho.rent.features.property.domain.model.Property>?
    )

    override fun onAction(action: TenantsListAction) {
        when (action) {
            is TenantsListAction.OnStatusFilterChanged -> intent {
                reduce { state.copy(selectedStatus = action.status) }
            }

            is TenantsListAction.OnPropertyFilterChanged -> intent {
                reduce { state.copy(selectedProperty = action.propertyName) }
            }

            is TenantsListAction.OnTenantClicked -> intent {
                postSideEffect(TenantsListSideEffect.NavigateToTenantDetails(action.tenantId))
            }

            is TenantsListAction.OnRecordPaymentClicked -> intent {
                postSideEffect(TenantsListSideEffect.NavigateToAddPayment(action.tenantId, action.propertyId))
            }

            is TenantsListAction.OnArchivedTenantBackupCompleted -> intent(registerIdling = false) {
                when (val result = deleteTenant(action.tenantId)) {
                    is com.skydoves.sandwich.ApiResponse.Success -> {
                        postSideEffect(TenantsListSideEffect.ShowMessage("Tenant deleted successfully"))
                        // Re-check for other archived tenants if necessary
                        checkArchivedTenants()
                    }
                    is com.skydoves.sandwich.ApiResponse.Failure.Error -> {
                        postSideEffect(TenantsListSideEffect.ShowError("Failed to delete tenant"))
                    }
                    is com.skydoves.sandwich.ApiResponse.Failure.Exception -> {
                        postSideEffect(TenantsListSideEffect.ShowError("Failed to delete tenant"))
                    }
                }
            }

            is TenantsListAction.OnRetry -> {
                observeProperties()
            }
        }
    }

    private fun observeProperties() = intent(registerIdling = false) {
        propertyRepository.getProperties(ownerId)
            .catch { e ->
                reduce { state.copy(propertiesState = UiState.Error("Failed to load properties")) }
            }
            .collect { properties ->
                reduce { state.copy(propertiesState = UiState.Success(properties.toImmutableList())) }
            }
    }
    
    private fun checkArchivedTenants() = intent(registerIdling = false) {
        val archived = getArchivedTenants(ownerId)
        if (archived.isNotEmpty()) {
            val tenant = archived.first()
            val profileInfo = "Name: ${tenant.name}\nPhone: ${tenant.phone ?: "N/A"}\nEmail: ${tenant.email ?: "N/A"}\nMoved In: ${tenant.moveInDate ?: "N/A"}"
            val rentInfo = "Rent Amount: ${tenant.rentAmount}\nSecurity Deposit: ${tenant.securityDeposit}\nProperty: ${tenant.propertyName ?: "N/A"}\nUnit: ${tenant.roomNumber ?: "N/A"}"
            postSideEffect(TenantsListSideEffect.ShowArchivedPrompt(tenant.id, tenant.name, profileInfo, rentInfo))
        }
    }

    private fun mapToDisplayModel(tenant: Tenant): TenantDisplayModel {
        val isActive = tenant.status.equals("Active", ignoreCase = true)
        val (bgColor, textColor) = TenantUtils.getAvatarColors(tenant.name)
        val initials = TenantUtils.getInitials(tenant.name)
        val subtitle = buildString {
            append(tenant.propertyName ?: "Assigned Room")
            if (!tenant.roomNumber.isNullOrBlank()) append(" · ${tenant.roomNumber}")
        }
        return TenantDisplayModel(
            id = tenant.id, name = tenant.name, initials = initials,
            subtitle = subtitle, status = tenant.status, isActive = isActive,
            avatarBgColorHex = bgColor, avatarTextColorHex = textColor,
            propertyName = tenant.propertyName, propertyId = tenant.propertyId, roomNumber = tenant.roomNumber,
            email = tenant.email, phone = tenant.phone,
            rentAmount = tenant.rentAmount
        )
    }
}
