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
        observeTenants()
        observeProperties()
    }

    override fun onAction(action: TenantsListAction) {
        when (action) {
            is TenantsListAction.OnSearchQueryChanged -> intent {
                val newState = state.copy(searchQuery = action.query)
                reduce { newState.copy(filteredTenants = computeFilteredTenants(newState)) }
            }
            is TenantsListAction.OnStatusFilterChanged -> intent {
                val newState = state.copy(selectedStatus = action.status)
                reduce { newState.copy(filteredTenants = computeFilteredTenants(newState)) }
            }
            is TenantsListAction.OnPropertyFilterChanged -> intent {
                val newState = state.copy(selectedProperty = action.propertyName)
                reduce { newState.copy(filteredTenants = computeFilteredTenants(newState)) }
            }
            is TenantsListAction.OnTenantClicked -> intent {
                postSideEffect(TenantsListSideEffect.NavigateToTenantDetails(action.tenantId))
            }
            is TenantsListAction.OnRetry -> {
                observeTenants()
                observeProperties()
            }
        }
    }

    private fun observeTenants() = intent(registerIdling = false) {
        reduce { state.copy(tenantsState = UiState.Loading) }
        tenantRepository.getTenants(ownerId)
            // IO dispatching is handled by LocalTenantRepository.flowOn(Dispatchers.IO)
            .catch { e ->
                val msg = ErrorMessageExtractor.extract(e, "Could not load tenants. Please try again.")
                reduce { state.copy(tenantsState = UiState.Error(msg)) }
                postSideEffect(TenantsListSideEffect.ShowError(msg))
            }
            .collect { tenants ->
                if (tenants.isEmpty()) {
                    seedInitialTenantsIfEmpty()
                } else {
                    val displayModels = tenants.map { mapToDisplayModel(it) }.toImmutableList()
                    val newState = state.copy(tenantsState = UiState.Success(displayModels))
                    reduce { newState.copy(filteredTenants = computeFilteredTenants(newState)) }
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

    /**
     * Pure filtering function — runs on the Orbit Default dispatcher, never on Main.
     * The result is stored as a state field so Compose reads zero logic on recomposition.
     */
    private fun computeFilteredTenants(s: TenantsListState): ImmutableList<TenantDisplayModel> {
        val raw = (s.tenantsState as? UiState.Success)?.data ?: return persistentListOf()
        return raw.filter { tenant ->
            val matchesSearch = if (s.searchQuery.isBlank()) {
                true
            } else {
                val q = s.searchQuery.trim().lowercase()
                tenant.name.lowercase().contains(q) ||
                    (tenant.email?.lowercase()?.contains(q) == true) ||
                    (tenant.phone?.lowercase()?.contains(q) == true) ||
                    (tenant.propertyName?.lowercase()?.contains(q) == true) ||
                    (tenant.roomNumber?.lowercase()?.contains(q) == true)
            }
            val matchesStatus = when (s.selectedStatus) {
                "All statuses" -> true
                else -> tenant.status.equals(s.selectedStatus, ignoreCase = true)
            }
            val matchesProperty = when (s.selectedProperty) {
                "All properties" -> true
                else -> tenant.propertyName?.equals(s.selectedProperty, ignoreCase = true) == true
            }
            matchesSearch && matchesStatus && matchesProperty
        }.toImmutableList()
    }

    private fun mapToDisplayModel(tenant: Tenant): TenantDisplayModel {
        val isActive = tenant.status.equals("Active", ignoreCase = true)
        val colors = com.gaatho.rent.core.designsystem.ExtendedColorHex.AvatarPairs
        val index = kotlin.math.abs(tenant.name.hashCode()) % colors.size
        val (bgColor, textColor) = colors[index]
        val parts = tenant.name.trim().split(Regex("\\s+"))
        val initials = if (parts.size >= 2) {
            "${parts[0].firstOrNull()?.uppercaseChar() ?: ""}${parts[1].firstOrNull()?.uppercaseChar() ?: ""}"
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

    private suspend fun seedInitialTenantsIfEmpty() {
        val now = DateTimeUtil.nowIsoString()
        val sampleTenants = listOf(
            Tenant(id = UuidUtil.randomGuestId(), ownerId = ownerId, name = "Anita Basnet", propertyName = "Sunrise Residency", roomNumber = "Room 4A", status = "Active", createdAt = now, updatedAt = now),
            Tenant(id = UuidUtil.randomGuestId(), ownerId = ownerId, name = "Bikash Lama", propertyName = "Ganga Nivas", roomNumber = "Room 5", status = "Active", createdAt = now, updatedAt = now),
            Tenant(id = UuidUtil.randomGuestId(), ownerId = ownerId, name = "Bipin Karki", propertyName = "Sunrise Residency", roomNumber = "Room 1A", status = "Active", createdAt = now, updatedAt = now),
            Tenant(id = UuidUtil.randomGuestId(), ownerId = ownerId, name = "Deepak Thapa", propertyName = "Ganga Nivas", roomNumber = "Room 1", status = "Active", createdAt = now, updatedAt = now),
            Tenant(id = UuidUtil.randomGuestId(), ownerId = ownerId, name = "Kamala Shrestha", propertyName = "Ganga Nivas", roomNumber = "Room 4", status = "Active", createdAt = now, updatedAt = now),
            Tenant(id = UuidUtil.randomGuestId(), ownerId = ownerId, name = "Manoj Poudel", propertyName = "Ganga Nivas", roomNumber = "Room 3", status = "Active", createdAt = now, updatedAt = now),
            Tenant(id = UuidUtil.randomGuestId(), ownerId = ownerId, name = "Nisha Tamang", propertyName = "Sunrise Residency", roomNumber = "Room 3A", status = "Active", createdAt = now, updatedAt = now),
            Tenant(id = UuidUtil.randomGuestId(), ownerId = ownerId, name = "Prakash Adhikari", propertyName = "Sunrise Residency", roomNumber = "Room 2A", status = "Active", createdAt = now, updatedAt = now),
            Tenant(id = UuidUtil.randomGuestId(), ownerId = ownerId, name = "Ramesh Koirala", propertyName = "Sunrise Residency", roomNumber = "Room 1B", status = "Active", createdAt = now, updatedAt = now),
            Tenant(id = UuidUtil.randomGuestId(), ownerId = ownerId, name = "Sita Gurung", propertyName = "Ganga Nivas", roomNumber = "Room 2", status = "Active", createdAt = now, updatedAt = now),
            Tenant(id = UuidUtil.randomGuestId(), ownerId = ownerId, name = "Suresh Shrestha", propertyName = "Sunrise Residency", roomNumber = "Room 5B", status = "Inactive", createdAt = now, updatedAt = now)
        )
        sampleTenants.forEach { tenantRepository.createTenant(it) }
    }
}
