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
        _searchQuery
            .debounce(300L)
            .distinctUntilChanged(),
        container.stateFlow
            .map { it.selectedFilter }
            .distinctUntilChanged(),
        tenantRepository.getTenants(ownerId)
    ) { debouncedSearch, filter, tenants ->
        Triple(debouncedSearch, filter, tenants)
    }
        .flatMapLatest { (search, filter, tenants) ->
            // Temporarily mapping filter to location filter if it's not "All"
            // In a real app, we'd update the repository to support type filters.
            val repoFilter = if (filter == PropertyListFilters.All) "" else filter
            repository.getPagedProperties(ownerId, search, repoFilter)
                .map { pagingData ->
                    pagingData.map { property ->
                        val propertyTenants = tenants.filter { it.propertyId == property.id }
                        val activeTenants = propertyTenants.filter { it.status == "Active" }
                        val overdueTenants = propertyTenants.filter { it.status == "Overdue" }

                        val occUnits = activeTenants.size + overdueTenants.size
                        val tUnits = property.totalUnits
                        val vac = maxOf(0, tUnits - occUnits)

                        val badge = if (tUnits == 0) "0 Units" else "$tUnits Units"

                        val pendingAmount = overdueTenants.sumOf { it.rentAmount }
                        val pText = if (pendingAmount > 0) "Rs. $pendingAmount Due" else "No Dues"

                        // Mock price based on name or units to match Figma if possible
                        val price = when {
                            property.name.contains("Baluwatar", ignoreCase = true) -> "1,25,000"
                            property.name.contains("Thamel", ignoreCase = true) -> "95,000"
                            property.name.contains("Baneshwor", ignoreCase = true) -> "80,000"
                            else -> "50,000"
                        }

                        PropertyDisplayModel(
                            id = property.id,
                            name = property.name,
                            address = property.address,
                            imageUrl = property.imageUrl,
                            totalUnits = tUnits,
                            occUnits = occUnits,
                            vacUnits = vac,
                            statusBadge = badge,
                            isVacant = vac > 0,
                            priceFormatted = price,
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
            is PropertyListAction.OnFilterSelected -> handleFilterSelect(action.filter)
            is PropertyListAction.OnQuickActionClicked -> handleQuickAction(action.message)
            is PropertyListAction.Retry -> handleRetry()
        }
    }

    private fun handleFilterSelect(filter: String) = intent {
        reduce { state.copy(selectedFilter = filter) }
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
