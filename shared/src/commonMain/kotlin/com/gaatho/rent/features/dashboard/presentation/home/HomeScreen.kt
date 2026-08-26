package com.gaatho.rent.features.dashboard.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.designsystem.Spacing
import com.gaatho.rent.core.ui.components.OfflineBanner
import com.gaatho.rent.features.dashboard.presentation.components.DashboardActivityUi
import com.gaatho.rent.features.dashboard.presentation.components.DashboardCollectionCard
import com.gaatho.rent.features.dashboard.presentation.components.DashboardMetricsRow
import com.gaatho.rent.features.dashboard.presentation.components.DashboardQuickActions
import com.gaatho.rent.features.dashboard.presentation.components.DashboardRecentActivity
import com.gaatho.rent.features.dashboard.presentation.components.DashboardWelcomeHeader
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectSideEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(
    onNavigateToAddTenant: () -> Unit = {},
    onNavigateToAddProperty: () -> Unit = {},
    onNavigateToAddPayment: () -> Unit = {},
    onNavigateToPayments: () -> Unit = {},
    onNavigateToTenantDetails: (String) -> Unit = {}
) {
    val viewModel: HomeViewModel = koinViewModel()
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is HomeSideEffect.NavigateToAddTenant -> onNavigateToAddTenant()
            is HomeSideEffect.NavigateToAddProperty -> onNavigateToAddProperty()
            is HomeSideEffect.NavigateToAddPayment -> onNavigateToAddPayment()
            is HomeSideEffect.NavigateToPayments -> onNavigateToPayments()
            is HomeSideEffect.NavigateToTenantDetails -> onNavigateToTenantDetails(effect.tenantId)
            is HomeSideEffect.NavigateToExpenses -> Unit
        }
    }

    HomeContent(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun HomeContent(
    state: HomeState,
    onAction: (HomeAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
    ) {
        OfflineBanner()
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Spacing.ScreenPadding,
                end = Spacing.ScreenPadding,
                top = 28.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { DashboardWelcomeHeader(userName = state.userName) }

            if (state.isLoading) {
                item { HomeSkeletonLoadingState() }
            } else {
                item {
                    DashboardCollectionCard(
                        collectedRent = state.collectedRent,
                        totalRent = state.totalRent
                    )
                }
                item {
                    DashboardMetricsRow(
                        propertiesCount = state.propertiesCount,
                        tenantsCount = state.tenantsCount,
                        overdueTenantsCount = state.overdueTenantsCount
                    )
                }
                item {
                    DashboardQuickActions(
                        onAddTenant = { onAction(HomeAction.OnAddTenantClicked) },
                        onRecordPayment = { onAction(HomeAction.OnRecordPaymentClicked) },
                        onReminder = { onAction(HomeAction.OnSeeAllPaymentsClicked) }
                    )
                }
                item {
                    DashboardRecentActivity(
                        activities = state.recentPayments.map { payment ->
                            DashboardActivityUi(
                                title = payment.tenantName,
                                subtitle = payment.propertyName,
                                amount = payment.amount,
                                dateLabel = payment.dateLabel,
                                isPositive = payment.isPaid
                            )
                        },
                        onSeeAll = { onAction(HomeAction.OnSeeAllPaymentsClicked) },
                        onActivityClick = { index ->
                            state.recentPayments.getOrNull(index)?.let { payment ->
                                onAction(HomeAction.OnRecentPaymentClicked(payment.tenantId))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeSkeletonLoadingState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        com.gaatho.rent.core.ui.components.AppShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(24.dp))
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) {
                com.gaatho.rent.core.ui.components.AppShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }
        }
        com.gaatho.rent.core.ui.components.AppShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(14.dp))
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenLightPreview() {
    RentManagerTheme(darkTheme = false) {
        HomeContent(
            state = HomeState(
                userName = "Ramesh ji",
                propertiesCount = 4,
                tenantsCount = 12,
                collectedRent = 245000L,
                totalRent = 320000L,
                overdueTenantsCount = 3,
                isLoading = false,
                recentPayments = persistentListOf(
                    RecentPaymentItem(
                        tenantId = "1",
                        tenantName = "Suman Maharjan",
                        propertyName = "Baluwatar House",
                        dateLabel = "Today",
                        amount = 25000L,
                        isPaid = true
                    ),
                    RecentPaymentItem(
                        tenantId = "2",
                        tenantName = "Anil Shrestha",
                        propertyName = "Baneshwor Shop",
                        dateLabel = "Yesterday",
                        amount = 40000L,
                        isPaid = true
                    ),
                    RecentPaymentItem(
                        tenantId = "3",
                        tenantName = "Sent Reminder",
                        propertyName = "To Rajesh Thapa",
                        dateLabel = "2 days ago",
                        amount = 18500L,
                        isPaid = false
                    )
                )
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenDarkPreview() {
    RentManagerTheme(darkTheme = true) {
        HomeContent(
            state = HomeState(
                userName = "Ramesh ji",
                propertiesCount = 4,
                tenantsCount = 12,
                collectedRent = 245000L,
                totalRent = 320000L,
                overdueTenantsCount = 3,
                isLoading = false
            ),
            onAction = {}
        )
    }
}
