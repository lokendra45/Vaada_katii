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
import androidx.paging.PagingData
import androidx.paging.map
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.lifecycle.viewModelScope

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
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedPropertiesFlow: Flow<PagingData<PropertyDisplayModel>> = combine(
        container.stateFlow
            .map { state -> Pair(state.searchQuery, state.selectedLocation) }
            .distinctUntilChanged(),
        tenantRepository.getTenants(ownerId)
    ) { (search, location), tenants ->
        Triple(search, location, tenants)
    }
        .flatMapLatest { (search, location, tenants) ->
            val locationFilter = if (location == "All properties") "" else location
            repository.getPagedProperties(ownerId, search, locationFilter)
                .map { pagingData ->
                    pagingData.map { property ->
                        val propertyTenants = tenants.filter { it.propertyId == property.id }
                        val activeTenants = propertyTenants.filter { it.status == "Active" }
                        val overdueTenants = propertyTenants.filter { it.status == "Overdue" }

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
                    }
                }
        }
        .cachedIn(viewModelScope)

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

    private fun handleSearchQueryChange(query: String) = intent {
        reduce { state.copy(searchQuery = query) }
    }

    private fun handleLocationFilterSelect(location: String) = intent {
        reduce { state.copy(selectedLocation = location) }
    }

    private fun handleRetry() = intent {
        // Handled by UI retry logic
    }

    private fun handlePropertyClick(propertyId: String) = intent {
        postSideEffect(PropertyListSideEffect.NavigateToDetails(propertyId))
    }

    private fun handleAddPropertyClick() = intent {
        postSideEffect(PropertyListSideEffect.NavigateToAddProperty)
    }



    private fun handleQuickAction(message: String) = intent {
        postSideEffect(PropertyListSideEffect.ShowMessage(message))
    }



}
