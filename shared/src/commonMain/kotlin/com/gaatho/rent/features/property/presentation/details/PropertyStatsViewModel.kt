package com.gaatho.rent.features.property.presentation.details

import com.gaatho.rent.core.auth.UserIdentityProvider
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.gaatho.rent.features.tenant.domain.model.Tenant
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.orbitmvi.orbit.viewmodel.orbitContainer

class PropertyStatsViewModel(
    private val propertyId: String,
    private val tenantRepository: TenantRepository,
    private val userIdentityProvider: UserIdentityProvider,
) : MviViewModel<PropertyStatsState, PropertyStatsEffect, PropertyStatsAction>() {

    override val container = orbitContainer<PropertyStatsState, PropertyStatsEffect>(
        initialState = PropertyStatsState(propertyId = propertyId)
    ) {
        observeData()
    }

    private fun observeData() = intent {
        val ownerId = userIdentityProvider.currentUserId()
        tenantRepository.getTenantsByProperty(ownerId, propertyId)
            .catch { e ->
                reduce { state.copy(financialState = UiState.Error(ErrorMessageExtractor.extract(e, "Failed to load stats"))) }
            }
            .collect { tenants ->
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

    override fun onAction(action: PropertyStatsAction) {}
}
