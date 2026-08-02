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

class PropertyListViewModel(
    private val repository: PropertyRepository,
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
        repository.getProperties(ownerId)
            .catch { e ->
                val msg = ErrorMessageExtractor.extract(e, "Could not load your properties. Please try again.")
                reduce { state.copy(propertiesState = UiState.Error(msg)) }
                postSideEffect(PropertyListSideEffect.ShowError(msg))
            }
            .collect { properties ->
                if (properties.isEmpty()) {
                    seedInitialPropertiesIfEmpty()
                } else {
                    val displayModels = properties.map { mapToDisplayModel(it) }.toImmutableList()
                    val newState = state.copy(propertiesState = UiState.Success(displayModels))
                    reduce { newState.copy(filteredProperties = computeFilteredProperties(newState)) }
                }
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

    private fun mapToDisplayModel(property: Property): PropertyDisplayModel {
        return when {
            property.name.contains("Lalitpur", ignoreCase = true) || property.name.contains("Heights", ignoreCase = true) ->
                PropertyDisplayModel(property.id, property.name, property.address, 24, 22, "• 2 Vacant", true, "Rs. 25,000 (2 Units)", true)
            property.name.contains("Baneshwor", ignoreCase = true) ->
                PropertyDisplayModel(property.id, property.name, property.address, 12, 12, "• Fully Occupied", false, "Fully Paid", false)
            property.name.contains("Shivapuri", ignoreCase = true) ->
                PropertyDisplayModel(property.id, property.name, property.address, 8, 8, "• Fully Occupied", false, "Rs. 12,500 (1 Unit)", true)
            else -> {
                val hash = kotlin.math.abs(property.id.hashCode() + property.name.hashCode())
                val tUnits = (hash % 12) + 6
                val vac = if (hash % 3 == 0) (hash % 2) + 1 else 0
                val oUnits = tUnits - vac
                val badge = if (vac > 0) "• $vac Vacant" else "• Fully Occupied"
                val pUnits = if (vac > 0) vac else (if (hash % 2 == 0) 0 else 1)
                val pText = if (pUnits > 0) "Rs. ${pUnits * 14},000 ($pUnits Unit${if (pUnits > 1) "s" else ""})" else "Fully Paid"
                PropertyDisplayModel(property.id, property.name, property.address, tUnits, oUnits, badge, vac > 0, pText, pUnits > 0)
            }
        }
    }

    private suspend fun seedInitialPropertiesIfEmpty() {
        val now = DateTimeUtil.nowIsoString()
        val sampleProperties = listOf(
            Property(id = UuidUtil.randomGuestId(), ownerId = ownerId, name = "Sunrise Residency", address = "Sanepa, Lalitpur", propertyType = "APARTMENT", createdAt = now, updatedAt = now),
            Property(id = UuidUtil.randomGuestId(), ownerId = ownerId, name = "Ganga Nivas", address = "Baneshwor, Kathmandu", propertyType = "HOUSE", createdAt = now, updatedAt = now),
            Property(id = UuidUtil.randomGuestId(), ownerId = ownerId, name = "Shivapuri View Appts", address = "Bansbari, Kathmandu", propertyType = "APARTMENT", createdAt = now, updatedAt = now),
            Property(id = UuidUtil.randomGuestId(), ownerId = ownerId, name = "Lalitpur Heights", address = "Jhamsikhel, Lalitpur", propertyType = "BUILDING", createdAt = now, updatedAt = now)
        )
        sampleProperties.forEach { repository.createProperty(it) }
    }
}
