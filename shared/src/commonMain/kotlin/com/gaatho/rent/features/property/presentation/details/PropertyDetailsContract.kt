package com.gaatho.rent.features.property.presentation.details

import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.features.property.domain.model.Property
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

// ─── Unit display model ───────────────────────────────────────────────────────

@Serializable
data class UnitDisplayModel(
    val unitNumber: String,         // e.g. "1A", "2B"
    val tenantName: String?,        // null = vacant
    val rentPerMonth: Long,         // NPR
    val paymentStatus: UnitPaymentStatus,
)

@Serializable
enum class UnitPaymentStatus { PAID, OVERDUE, VACANT }

// ─── Financial summary ────────────────────────────────────────────────────────

@Serializable
data class FinancialSummary(
    val currentMonth: String,       // e.g. "Asar"
    val totalCollected: Long,       // NPR
    val outstandingDues: Long,      // NPR
)

// ─── Main state ───────────────────────────────────────────────────────────────

data class PropertyDetailsState(
    val propertyId: String = "",
    val showDeleteConfirm: Boolean = false,
    val isDeleting: Boolean = false
)

// ─── Actions ──────────────────────────────────────────────────────────────────

sealed interface PropertyDetailsAction {
    data object OnBackClicked : PropertyDetailsAction
    data object OnEditClicked : PropertyDetailsAction
    data object OnAddTenantClicked : PropertyDetailsAction
    data object OnDeleteClicked : PropertyDetailsAction
    data object OnDeleteConfirmed : PropertyDetailsAction
    data object OnDeleteDismissed : PropertyDetailsAction
data class OnUnitClicked(val unitNumber: String) : PropertyDetailsAction
    data object OnViewAllUnitsClicked : PropertyDetailsAction
}

// ─── Side effects ─────────────────────────────────────────────────────────────

sealed interface PropertyDetailsSideEffect {
    data object NavigateBack : PropertyDetailsSideEffect
data class NavigateToEdit(val propertyId: String) : PropertyDetailsSideEffect
    data object NavigateToAddTenant : PropertyDetailsSideEffect
data class ShowError(val message: String) : PropertyDetailsSideEffect
}
