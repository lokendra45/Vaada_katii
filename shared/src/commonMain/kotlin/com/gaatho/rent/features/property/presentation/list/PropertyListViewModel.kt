package com.gaatho.rent.features.property.presentation.list

import androidx.lifecycle.SavedStateHandle
import com.gaatho.rent.core.auth.UserIdentityProvider
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.core.utils.UuidUtil
import com.gaatho.rent.features.property.domain.model.Property
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.catch
import org.orbitmvi.orbit.viewmodel.orbitContainer

import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.gaatho.rent.features.payment.domain.repository.PaymentRepository

class PropertyListViewModel(
    private val repository: PropertyRepository,
    private val tenantRepository: TenantRepository,
    private val paymentRepository: PaymentRepository,
    private val userIdentityProvider: UserIdentityProvider,
    savedStateHandle: SavedStateHandle
) : MviViewModel<PropertyListState, PropertyListSideEffect, PropertyListAction>() {

    /**
     * After [SqlDelightGuestSessionManager] caches the value on first access,
     * this property is a pure in-memory lookup — no DB hit, safe on any thread.
     * The repository layer owns IO dispatching for all actual DB operations.
     */
    private val ownerId: String
        get() = userIdentityProvider.currentUserId()

    /**
     * Orbit container with full saved state support.
     * The `onCreate` lambda starts [observeProperties]. Repository layer
     * guarantees all DB work runs on Dispatchers.IO via flowOn/withContext.
     */
    override val container = orbitContainer<PropertyListState, PropertyListSideEffect>(
        initialState = PropertyListState(),
        savedStateHandle = savedStateHandle,
        serializer = PropertyListState.serializer()
    ) {
        observeProperties()
    }

    override fun onAction(action: PropertyListAction) {
        when (action) {
            is PropertyListAction.OnPropertyClicked -> handlePropertyClick(action.propertyId)
            is PropertyListAction.OnAddPropertyClicked -> handleAddPropertyClick()
            is PropertyListAction.OnSearchQueryChanged -> handleSearchQueryChange(action.query)
            is PropertyListAction.OnLocationFilterSelected -> handleLocationFilterSelect(action.location)
            is PropertyListAction.OnQuickActionClicked -> handleQuickAction(action.message)
            is PropertyListAction.Retry -> handleRetry()
        }
    }

    /**
     * Long-lived reactive observer. Subscribes once via `onCreate`.
     * IO dispatching is handled by [LocalPropertyRepository] via flowOn(Dispatchers.IO).
     */
    private fun observeProperties() = intent(registerIdling = false) {
        val propertiesFlow = repository.getProperties(ownerId)
        val tenantsFlow = tenantRepository.getTenants(ownerId)
        val paymentsFlow = paymentRepository.getPaymentsByOwner(ownerId)

        kotlinx.coroutines.flow.combine(propertiesFlow, tenantsFlow, paymentsFlow) { properties, tenants, payments ->
            properties.map { property ->
                val propertyTenants = tenants.filter { it.propertyId == property.id }
                val activeTenants = propertyTenants.filter { it.status == "Active" }
                val overdueTenants = propertyTenants.filter { it.status == "Overdue" }

                // Use real totalUnits from database
                val occUnits = activeTenants.size + overdueTenants.size
                val tUnits = property.totalUnits
                val vac = maxOf(0, tUnits - occUnits)

                val badge = if (tUnits == 0) "• 0 Units" else if (vac > 0) "• $vac Vacant" else "• Fully Occupied"

                val pendingAmount = overdueTenants.sumOf { it.rentAmount }
                val pText = if (pendingAmount > 0) "Rs. $pendingAmount Due" else "No Dues"

                PropertyDisplayModel(
                    id = property.id,
                    name = property.name,
                    address = property.address,
                    imageUrl = property.imageUrl,
                    totalUnits = tUnits,
                    occUnits = occUnits,
                    statusBadge = badge,
                    isVacant = vac > 0,
                    pendingText = pText,
                    isPending = pendingAmount > 0
                )
            }.toImmutableList()
        }
        .catch { e ->
            val msg = ErrorMessageExtractor.extract(e, "Could not load your properties. Please try again.")
            reduce { state.copy(propertiesState = UiState.Error(msg)) }
            postSideEffect(PropertyListSideEffect.ShowError(msg))
        }
        .collect { displayModels ->
            val newState = state.copy(propertiesState = UiState.Success(displayModels))
            reduce { newState.copy(filteredProperties = computeFilteredProperties(newState)) }
        }
    }

    /**
     * Pure filtering — runs on the Orbit Default dispatcher (never Main).
     * Result stored as a state field so Compose reads zero logic on recomposition.
     */
    private fun computeFilteredProperties(s: PropertyListState): ImmutableList<PropertyDisplayModel> {
        val raw = (s.propertiesState as? UiState.Success)?.data ?: return persistentListOf()
        return raw.filter { prop ->
            val matchesSearch = if (s.searchQuery.isBlank()) {
                true
            } else {
                val q = s.searchQuery.trim().lowercase()
                prop.name.lowercase().contains(q) || prop.address.lowercase().contains(q)
            }
            val matchesLocation = if (s.selectedLocation == "All properties" || s.selectedLocation.isBlank()) {
                true
            } else {
                prop.address.lowercase().contains(s.selectedLocation.lowercase()) ||
                    prop.name.lowercase().contains(s.selectedLocation.lowercase())
            }
            matchesSearch && matchesLocation
        }.toImmutableList()
    }

    private fun handleRetry() = intent {
        reduce { state.copy(propertiesState = UiState.Loading) }
        observeProperties()
    }

    private fun handlePropertyClick(propertyId: String) = intent {
        postSideEffect(PropertyListSideEffect.NavigateToDetails(propertyId))
    }

    private fun handleAddPropertyClick() = intent {
        postSideEffect(PropertyListSideEffect.NavigateToAddProperty)
    }

    private fun handleSearchQueryChange(query: String) = intent {
        val newState = state.copy(searchQuery = query)
        reduce { newState.copy(filteredProperties = computeFilteredProperties(newState)) }
    }

    private fun handleLocationFilterSelect(location: String) = intent {
        val newState = state.copy(selectedLocation = location)
        reduce { newState.copy(filteredProperties = computeFilteredProperties(newState)) }
    }

    private fun handleQuickAction(message: String) = intent {
        postSideEffect(PropertyListSideEffect.ShowMessage(message))
    }



}
