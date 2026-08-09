package com.gaatho.rent.features.tenant.presentation.list

import androidx.lifecycle.SavedStateHandle
import com.gaatho.rent.core.auth.UserIdentityProvider
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.core.utils.UuidUtil
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.gaatho.rent.features.tenant.domain.model.Tenant
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.catch
import org.orbitmvi.orbit.viewmodel.orbitContainer
import androidx.paging.PagingData
import androidx.paging.map
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.lifecycle.viewModelScope

class TenantsListViewModel(
    private val tenantRepository: TenantRepository,
    private val propertyRepository: PropertyRepository,
    private val userIdentityProvider: UserIdentityProvider,
    savedStateHandle: SavedStateHandle
) : MviViewModel<TenantsListState, TenantsListSideEffect, TenantsListAction>() {

    /**
     * After [SqlDelightGuestSessionManager] caches the value on first access,
     * this property is a pure in-memory lookup — no DB hit, safe on any thread.
     * The repository layer owns IO dispatching for all actual DB operations.
     */
    private val ownerId: String
        get() = userIdentityProvider.currentUserId()

    override val container = orbitContainer<TenantsListState, TenantsListSideEffect>(
        initialState = TenantsListState(),
        savedStateHandle = savedStateHandle,
        serializer = TenantsListState.serializer()
    ) {
        observeProperties()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedTenantsFlow: Flow<PagingData<TenantDisplayModel>> = container.stateFlow
        .map { state ->
            FilterParams(
                search = state.searchQuery,
                status = state.selectedStatus,
                propertyName = state.selectedProperty,
                properties = (state.propertiesState as? UiState.Success)?.data
            )
        }
        .distinctUntilChanged()
        .flatMapLatest { params ->
            val statusFilter = if (params.status == "All statuses") "" else params.status
            val propertyId = if (params.propertyName == "All properties") ""
            else params.properties?.find { it.name == params.propertyName }?.id ?: ""

            tenantRepository.getPagedTenants(
                ownerId = ownerId,
                searchQuery = params.search,
                statusFilter = statusFilter,
                propertyId = propertyId
            ).map { pagingData ->
                pagingData.map { mapToDisplayModel(it) }
            }
        }
        .cachedIn(viewModelScope)

    private data class FilterParams(
        val search: String,
        val status: String,
        val propertyName: String,
        val properties: List<com.gaatho.rent.features.property.domain.model.Property>?
    )

    override fun onAction(action: TenantsListAction) {
        when (action) {
            is TenantsListAction.OnSearchQueryChanged -> intent {
                reduce { state.copy(searchQuery = action.query) }
            }

            is TenantsListAction.OnStatusFilterChanged -> intent {
                reduce { state.copy(selectedStatus = action.status) }
            }

            is TenantsListAction.OnPropertyFilterChanged -> intent {
                reduce { state.copy(selectedProperty = action.propertyName) }
            }

            is TenantsListAction.OnTenantClicked -> intent {
                postSideEffect(TenantsListSideEffect.NavigateToTenantDetails(action.tenantId))
            }

            is TenantsListAction.OnRetry -> {
                observeProperties()
            }
        }
    }



    private fun observeProperties() = intent(registerIdling = false) {
        propertyRepository.getProperties(ownerId)
            // IO dispatching is handled by LocalPropertyRepository.flowOn(Dispatchers.IO)
            .catch { e ->
                reduce { state.copy(propertiesState = UiState.Error("Failed to load properties")) }
            }
            .collect { properties ->
                reduce { state.copy(propertiesState = UiState.Success(properties.toImmutableList())) }
            }
    }



    private fun mapToDisplayModel(tenant: Tenant): TenantDisplayModel {
        val isActive = tenant.status.equals("Active", ignoreCase = true)
        val colors = com.gaatho.rent.core.designsystem.ExtendedColorHex.AvatarPairs
        val index = kotlin.math.abs(tenant.name.hashCode()) % colors.size
        val (bgColor, textColor) = colors[index]
        val parts = tenant.name.trim().split(Regex("\\s+"))
        val initials = if (parts.size >= 2) {
            "${parts[0].firstOrNull()?.uppercaseChar() ?: ""}${
                parts[1].firstOrNull()?.uppercaseChar() ?: ""
            }"
        } else {
            tenant.name.take(2).uppercase()
        }
        val subtitle = buildString {
            append(tenant.propertyName ?: "Assigned Room")
            if (!tenant.roomNumber.isNullOrBlank()) append(" · ${tenant.roomNumber}")
        }
        return TenantDisplayModel(
            id = tenant.id, name = tenant.name, initials = initials,
            subtitle = subtitle, status = tenant.status, isActive = isActive,
            avatarBgColorHex = bgColor, avatarTextColorHex = textColor,
            propertyName = tenant.propertyName, roomNumber = tenant.roomNumber,
            email = tenant.email, phone = tenant.phone
        )

    }
}
