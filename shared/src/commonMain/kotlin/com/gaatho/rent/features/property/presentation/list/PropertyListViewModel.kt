package com.gaatho.rent.features.property.presentation.list

import androidx.lifecycle.SavedStateHandle
import com.gaatho.rent.core.auth.UserIdentityProvider
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import kotlinx.coroutines.flow.catch
import org.orbitmvi.orbit.viewmodel.orbitContainer
import androidx.paging.PagingData
import androidx.paging.map
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.debounce
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

    private val ownerId: String
        get() = userIdentityProvider.currentUserId()

    /**
     * Search query lives in its own MutableStateFlow — NOT in Orbit state.
     *
     * This is the NiA / Google pattern: keeping the raw text field value
     * separate prevents Orbit from triggering a full recomposition on every
     * keystroke. The UI holds its own `var searchQuery` with mutableStateOf,
     * and calls [onSearchQueryChanged] directly — no Action dispatch needed.
     */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    override val container = orbitContainer<PropertyListState, PropertyListSideEffect>(
        initialState = PropertyListState(),
        savedStateHandle = savedStateHandle,
        serializer = PropertyListState.serializer()
    ) {}

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedPropertiesFlow: Flow<PagingData<PropertyDisplayModel>> = combine(
        // Debounce the raw search input — waits 300ms after last keystroke before querying DB.
        // distinctUntilChanged ensures identical queries don't restart the Pager.
        _searchQuery
            .debounce(300L)
            .distinctUntilChanged(),
        // Filters (location) still come from Orbit state — these are intentional taps, not rapid typing.
        container.stateFlow
            .map { it.selectedLocation }
            .distinctUntilChanged(),
        tenantRepository.getTenants(ownerId)
    ) { debouncedSearch, location, tenants ->
        Triple(debouncedSearch, location, tenants)
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
            is PropertyListAction.OnLocationFilterSelected -> handleLocationFilterSelect(action.location)
            is PropertyListAction.OnQuickActionClicked -> handleQuickAction(action.message)
            is PropertyListAction.Retry -> handleRetry()
        }
    }

    private fun handleLocationFilterSelect(location: String) = intent {
        reduce { state.copy(selectedLocation = location) }
    }

    private fun handleRetry() = intent { }

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
