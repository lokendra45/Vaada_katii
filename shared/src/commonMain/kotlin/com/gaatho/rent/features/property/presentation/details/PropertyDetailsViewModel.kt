package com.gaatho.rent.features.property.presentation.details

import com.gaatho.rent.core.auth.UserIdentityProvider
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.gaatho.rent.features.tenant.domain.model.Tenant
import com.skydoves.sandwich.ApiResponse
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.orbitmvi.orbit.viewmodel.orbitContainer

class PropertyDetailsViewModel(
    private val propertyId: String,
    private val propertyRepository: PropertyRepository,
    private val tenantRepository: TenantRepository,
    private val userIdentityProvider: UserIdentityProvider,
) : MviViewModel<PropertyDetailsState, PropertyDetailsSideEffect, PropertyDetailsAction>() {

    override val container = orbitContainer<PropertyDetailsState, PropertyDetailsSideEffect>(
        initialState = PropertyDetailsState()
    ) {
        observeData()
    }

    private fun observeData() = intent {
        val ownerId = userIdentityProvider.currentUserId()

        combine(
            propertyRepository.getProperties(ownerId),
            tenantRepository.getTenants(ownerId)
        ) { properties, tenants ->
            val property = properties.firstOrNull { it.id == propertyId }
            val propertyTenants = tenants.filter { it.propertyId == propertyId }
            Pair(property, propertyTenants)
        }
            .catch { e ->
                reduce {
                    state.copy(
                        propertyState = UiState.Error(e.message ?: "Failed to load property"),
                        unitsState = UiState.Error("Failed to load units"),
                        financialState = UiState.Error("Failed to load financials"),
                    )
                }
            }
            .collect { (property, tenants) ->
                val units = buildUnitList(tenants)
                val occupied = units.count { it.paymentStatus != UnitPaymentStatus.VACANT }
                val monthlyIncome = units
                    .filter { it.paymentStatus != UnitPaymentStatus.VACANT }
                    .sumOf { it.rentPerMonth }
                val totalCollected = units
                    .filter { it.paymentStatus == UnitPaymentStatus.PAID }
                    .sumOf { it.rentPerMonth }
                val outstanding = units
                    .filter { it.paymentStatus == UnitPaymentStatus.OVERDUE }
                    .sumOf { it.rentPerMonth }
                val currentMonth = kotlin.time.Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .month
                    .name
                    .lowercase()
                    .replaceFirstChar { it.uppercaseChar() }

                reduce {
                    state.copy(
                        propertyState = if (property != null) UiState.Success(property)
                        else UiState.Error("Property not found"),
                        unitsState = UiState.Success(units.toImmutableList()),
                        financialState = UiState.Success(
                            FinancialSummary(
                                currentMonth = currentMonth,
                                totalCollected = totalCollected,
                                outstandingDues = outstanding,
                            )
                        ),
                        monthlyIncome = monthlyIncome,
                        occupiedUnits = occupied,
                        totalUnits = units.size,
                    )
                }
            }
    }

    /** Maps tenants to unit display rows. Each tenant's roomNumber becomes the unit label. */
    private fun buildUnitList(tenants: List<Tenant>): List<UnitDisplayModel> {
        if (tenants.isEmpty()) return emptyList()
        return tenants.map { tenant ->
            val status = when {
                tenant.status.equals("Overdue", ignoreCase = true) -> UnitPaymentStatus.OVERDUE
                tenant.status.equals("Active", ignoreCase = true) -> UnitPaymentStatus.PAID
                else -> UnitPaymentStatus.VACANT
            }
            UnitDisplayModel(
                unitNumber = tenant.roomNumber ?: "—",
                tenantName = if (status == UnitPaymentStatus.VACANT) null else tenant.name,
                rentPerMonth = tenant.rentAmount,
                paymentStatus = status,
            )
        }
    }

    override fun onAction(action: PropertyDetailsAction) {
        when (action) {
            PropertyDetailsAction.OnBackClicked ->
                intent { postSideEffect(PropertyDetailsSideEffect.NavigateBack) }

            PropertyDetailsAction.OnEditClicked ->
                intent { postSideEffect(PropertyDetailsSideEffect.NavigateToEdit(propertyId)) }

            PropertyDetailsAction.OnAddTenantClicked ->
                intent { postSideEffect(PropertyDetailsSideEffect.NavigateToAddTenant) }

            PropertyDetailsAction.OnDeleteClicked ->
                intent { reduce { state.copy(showDeleteConfirm = true) } }

            PropertyDetailsAction.OnDeleteDismissed ->
                intent { reduce { state.copy(showDeleteConfirm = false) } }

            PropertyDetailsAction.OnDeleteConfirmed -> handleDelete()

            is PropertyDetailsAction.OnUnitClicked -> { /* navigate to unit detail */ }

            PropertyDetailsAction.OnViewAllUnitsClicked -> { /* navigate to unit list */ }
        }
    }

    private fun handleDelete() = intent {
        reduce { state.copy(isDeleting = true, showDeleteConfirm = false) }
        when (val result = propertyRepository.deleteProperty(propertyId)) {
            is ApiResponse.Success -> postSideEffect(PropertyDetailsSideEffect.NavigateBack)
            is ApiResponse.Failure.Error -> {
                reduce { state.copy(isDeleting = false) }
                postSideEffect(PropertyDetailsSideEffect.ShowError(
                    ErrorMessageExtractor.extract(result, "Failed to delete property")
                ))
            }
            is ApiResponse.Failure.Exception -> {
                reduce { state.copy(isDeleting = false) }
                postSideEffect(PropertyDetailsSideEffect.ShowError(
                    ErrorMessageExtractor.extract(result, "Failed to delete property")
                ))
            }
        }
    }
}
