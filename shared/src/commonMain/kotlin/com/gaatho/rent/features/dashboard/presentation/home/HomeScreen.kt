package com.gaatho.rent.features.dashboard.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.designsystem.Spacing
import com.gaatho.rent.features.dashboard.presentation.components.DashboardActivityUi
import com.gaatho.rent.features.dashboard.presentation.components.DashboardCollectionCardV2
import com.gaatho.rent.features.dashboard.presentation.components.DashboardHeaderV2
import com.gaatho.rent.features.dashboard.presentation.components.DashboardPropertiesList
import com.gaatho.rent.features.dashboard.presentation.components.DashboardPropertyUi
import com.gaatho.rent.features.dashboard.presentation.components.DashboardQuickActionsV2
import com.gaatho.rent.features.dashboard.presentation.components.DashboardRecentActivityV2
import com.gaatho.rent.features.dashboard.presentation.components.DashboardSegmentedControl
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectSideEffect
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.dashboard_footer_text

@Composable
fun HomeScreen(
    onNavigateToAddTenant: () -> Unit = {},
    onNavigateToAddProperty: () -> Unit = {},
    onNavigateToAddPayment: () -> Unit = {},
    onNavigateToPayments: () -> Unit = {},
    onNavigateToProperties: () -> Unit = {},
    onNavigateToTenantDetails: (String) -> Unit = {}
) {
    val viewModel: HomeViewModel = koinViewModel()
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()

    viewModel.collectSideEffect(lifecycleState = androidx.lifecycle.Lifecycle.State.RESUMED) { effect ->
        when (effect) {
            is HomeSideEffect.NavigateToAddTenant -> onNavigateToAddTenant()
            is HomeSideEffect.NavigateToAddProperty -> onNavigateToAddProperty()
            is HomeSideEffect.NavigateToAddPayment -> onNavigateToAddPayment()
            is HomeSideEffect.NavigateToPayments -> onNavigateToPayments()
            is HomeSideEffect.NavigateToProperties -> onNavigateToProperties()
            is HomeSideEffect.NavigateToTenantDetails -> onNavigateToTenantDetails(effect.tenantId)
            is HomeSideEffect.NavigateToExpenses -> Unit
        }
    }

    HomeContent(
        state = state,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeState,
    onAction: (HomeAction) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    DashboardHeaderV2(
                        userName = state.userName,
                        modifier = Modifier.padding(end = Spacing.ScreenPadding)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        // Animated entry for the content
        AnimatedContent(
            targetState = state.isLoading,
            transitionSpec = {
                (fadeIn(animationSpec = tween(300)) + slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialOffsetY = { fullHeight -> fullHeight / 8 }
                )).togetherWith(fadeOut(animationSpec = tween(200)))
            },
            label = "home_content_animation"
        ) { isLoading ->
            if (isLoading) {
                HomeSkeletonLoadingState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Spacing.ScreenPadding,
                        end = Spacing.ScreenPadding,
                        top = innerPadding.calculateTopPadding() + 8.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    item {
                        DashboardCollectionCardV2(
                            collectedRent = state.collectedRent,
                            totalRent = state.totalRent,
                            chartData = if (state.chartData.isNotEmpty()) state.chartData else listOf(0f),
                            periodLabel = if (state.selectedPeriod == com.gaatho.rent.features.dashboard.presentation.home.DashboardPeriod.THIS_MONTH) "This Month" else "Last Month"
                        )
                    }
                    
                    item {
                        val propertyUis = remember(state.properties) {
                            state.properties.map {
                                DashboardPropertyUi(
                                    id = it.id,
                                    name = it.name,
                                    location = it.location,
                                    imageUrl = it.imageUrl,
                                    totalUnits = it.totalUnits,
                                    occupiedUnits = it.occupiedUnits
                                )
                            }
                        }
                        DashboardPropertiesList(
                            properties = propertyUis,
                            onAddProperty = { onAction(HomeAction.OnAddPropertyClicked) },
                            onSeeAll = { onAction(HomeAction.OnSeeAllPropertiesClicked) }
                        )
                    }
                    
                    item {
                        DashboardQuickActionsV2(
                            onRecordPayment = { onAction(HomeAction.OnRecordPaymentClicked) },
                            onReminder = { onAction(HomeAction.OnSeeAllPaymentsClicked) },
                            onAddTenant = { onAction(HomeAction.OnAddTenantClicked) }
                        )
                    }
                    
                    item {
                        DashboardRecentActivityV2(
                            activities = remember(state.recentPayments) {
                                state.recentPayments.map { payment ->
                                    DashboardActivityUi(
                                        title = payment.tenantName,
                                        subtitle = payment.propertyName,
                                        amount = payment.amount,
                                        dateLabel = payment.dateLabel,
                                        isPositive = payment.isPaid
                                    )
                                }
                            },
                            onSeeAll = { onAction(HomeAction.OnSeeAllPaymentsClicked) },
                            onActivityClick = { index ->
                                state.recentPayments.getOrNull(index)?.let { payment ->
                                    onAction(HomeAction.OnRecentPaymentClicked(payment.tenantId))
                                }
                            }
                        )
                    }
                    
                    item {
                        Text(
                            text = stringResource(Res.string.dashboard_footer_text),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSkeletonLoadingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.ScreenPadding, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        com.gaatho.rent.core.ui.components.AppShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(24.dp))
        )
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
