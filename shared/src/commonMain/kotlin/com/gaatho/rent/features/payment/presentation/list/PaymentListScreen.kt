package com.gaatho.rent.features.payment.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.AppDimensions
import com.gaatho.rent.core.designsystem.ExtendedColorHex
import com.gaatho.rent.core.designsystem.Radius
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.designsystem.Spacing
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.ui.components.AppSearchBar
import com.gaatho.rent.core.ui.components.AppSegmentedControl
import com.gaatho.rent.core.ui.components.AppTopBar
import org.koin.compose.koinInject
import org.orbitmvi.orbit.compose.collectAsState
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.*
import rentmanagerapp.shared.generated.resources.empty_payments

@Composable
fun PaymentListScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPaymentDetails: (String) -> Unit,
    onNavigateToAddPayment: () -> Unit = {},
    viewModel: PaymentListViewModel = koinInject()
) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.container.sideEffectFlow) {
        viewModel.container.sideEffectFlow.collect { effect ->
            when (effect) {
                is PaymentListSideEffect.NavigateBack -> onNavigateBack()
                is PaymentListSideEffect.NavigateToPaymentDetails -> onNavigateToPaymentDetails(
                    effect.paymentId
                )
                is PaymentListSideEffect.NavigateToAddPayment -> onNavigateToAddPayment()
            }
        }
    }

    PaymentListContent(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentListContent(
    state: PaymentListState,
    onAction: (PaymentListAction) -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Payment History",
                onBackClick = { onAction(PaymentListAction.OnBackClicked) },
                actions = {
                    com.gaatho.rent.core.ui.components.AppTopBarActionButton(
                        text = "Add Payment",
                        onClick = { onAction(PaymentListAction.OnAddPaymentClicked) }
                    )
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) { paddings ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddings)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 800.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                // Search & Filter Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = Spacing.ScreenPadding, end = Spacing.ScreenPadding, top = 8.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppSearchBar(
                        query = state.searchQuery,
                        onQueryChange = { onAction(PaymentListAction.OnSearchQueryChanged(it)) },
                        placeholderText = "Search payments",
                        suggestions = emptyList(),
                        onSuggestionSelected = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    val options = listOf("All statuses", "Paid", "Overdue", "Pending")
                    val selectedIndex = options.indexOf(state.selectedStatus).coerceAtLeast(0)
                    
                    AppSegmentedControl(
                        options = options,
                        selectedIndex = selectedIndex,
                        onOptionSelected = { index -> 
                            onAction(PaymentListAction.OnStatusFilterChanged(options[index])) 
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Content
            when (val s = state.paymentsState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = s.message, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { onAction(PaymentListAction.OnRetry) }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    if (s.data.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            com.gaatho.rent.core.ui.components.AppIllustratedEmptyState(
                                illustration = Res.drawable.empty_payments,
                                title = "No Payments Found",
                                description = "You haven't recorded any payments yet.",
                                buttonText = "Record Payment",
                                onButtonClick = { onAction(PaymentListAction.OnAddPaymentClicked) }
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = Spacing.ScreenPadding,
                                end = Spacing.ScreenPadding,
                                top = 8.dp,
                                bottom = 24.dp
                            )
                        ) {
                            items(s.data, key = { it.id }) { payment ->
                                PaymentRowItem(
                                    payment = payment,
                                    onClick = { onAction(PaymentListAction.OnPaymentClicked(payment.id)) }
                                )
                            }
                        }
                    }
                }
                UiState.Idle -> {}
            }
            }
        }
    }
}

@Composable
private fun PaymentRowItem(
    payment: PaymentDisplayModel,
    onClick: () -> Unit
) {
    val initials = payment.tenantName.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")
    val avatarColors = com.gaatho.rent.core.utils.TenantUtils.getAvatarColors(payment.tenantName)

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(Radius.Md),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Avatar (Finzo style)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(avatarColors.first)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(avatarColors.second),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Name + subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = payment.tenantName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${payment.propertyName} · ${payment.dateLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Amount + status
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Rs ${payment.amount}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (payment.isPaid) AppColors.Success else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = payment.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
