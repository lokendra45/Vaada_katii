package com.gaatho.rent.features.property.presentation.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.utils.MoneyUtil
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.property.domain.model.Property
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
import kotlinx.coroutines.flow.update
import org.orbitmvi.orbit.viewmodel.orbitContainer
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
class PropertyListViewModel(
    private val repository: PropertyRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : MviViewModel<PropertyListState, PropertyListSideEffect, PropertyListAction>() {

    private val ownerId: String
        get() = (sessionManager.currentUserId() ?: "")

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchText.update { query }
    }

    override val container = orbitContainer<PropertyListState, PropertyListSideEffect>(
        initialState = PropertyListState(),
        savedStateHandle = savedStateHandle,
        serializer = PropertyListState.serializer()
    ) {}

    /**
     * Paginated property list. Occupancy counts and pending dues come directly
     * from SQL aggregation — no longer requires loading all tenants into memory.
     */
    val pagedPropertiesFlow: Flow<PagingData<PropertyDisplayModel>> = combine(
        _searchText
            .debounce(500L.milliseconds)
            .onEach { _isSearching.value = true }
            .distinctUntilChanged(),
        container.stateFlow
            .map { it.selectedFilter }
            .distinctUntilChanged()
    ) { search, filter ->
        search to filter
    }
        .flatMapLatest { pair ->
            val search = pair.first
            val filter = pair.second
            val repoFilter = if (filter == PropertyListFilters.All) "" else filter

            repository.getPagedProperties(ownerId, search, repoFilter)
                .map { pagingData ->
                    _isSearching.value = false
                    pagingData.map { property -> property.toDisplayModel() }
                }.catch { e ->
                    _isSearching.value = false
                    AppLogger.network.e(e) { "Properties paging flow failed" }
                    emit(PagingData.empty())
                }
        }
        .cachedIn(viewModelScope)

    private fun Property.toDisplayModel(): PropertyDisplayModel {
        val occUnits = occupiedUnits
        val tUnits = totalUnits
        val vac = maxOf(0, tUnits - occUnits)
        val badge = if (tUnits == 0) "0 Units" else "$tUnits Units"
        
        // Use MoneyUtil for standardized financial formatting (Paisa -> Rupees display)
        val pText = if (pendingAmount > 0) "${MoneyUtil.format(pendingAmount, includeSymbol = false)} Due" else "No Dues"

        return PropertyDisplayModel(
            id = id,
            name = name,
            address = address,
            imageUrl = imageUrl,
            totalUnits = tUnits,
            occUnits = occUnits,
            vacUnits = vac,
            statusBadge = badge,
            isVacant = vac > 0,
            priceFormatted = MoneyUtil.format(monthlyRent, includeSymbol = false),
            pendingText = pText,
            isPending = pendingAmount > 0
        )
    }

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
