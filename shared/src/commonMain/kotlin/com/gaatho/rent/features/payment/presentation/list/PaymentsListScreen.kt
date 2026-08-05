package com.gaatho.rent.features.payment.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.AppDimensions
import com.gaatho.rent.core.designsystem.ExtendedColorHex
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.ui.components.AppSearchBar
import com.gaatho.rent.core.ui.components.AppSegmentedControl
import com.gaatho.rent.core.ui.components.EmptyStateCard
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.*

@Composable
fun PaymentsListScreen(
    onNavigateToAddPayment: () -> Unit,
    onNavigateToDetails: (String) -> Unit = {}
) {
    val viewModel: PaymentsListViewModel = koinViewModel()
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PaymentsListSideEffect.NavigateToPaymentDetails ->
                onNavigateToDetails(sideEffect.paymentId)
            is PaymentsListSideEffect.NavigateToAddPayment ->
                onNavigateToAddPayment()
        }
    }

    PaymentsListContent(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun PaymentsListContent(
    state: PaymentsListState,
    onAction: (PaymentsListAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            com.gaatho.rent.core.ui.components.AppTopBar(
                title = stringResource(Res.string.payments_title),
                subtitle = "${state.totalCount} total",
                actions = {
                    com.gaatho.rent.core.ui.components.AppTopBarActionButton(
                        text = stringResource(Res.string.add_payment),
                        onClick = { onAction(PaymentsListAction.OnAddPayment) }
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 800.dp)
                    .align(Alignment.TopCenter)
            ) {
                // 1. Search & Filter Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppSearchBar(
                        query = state.searchQuery,
                        onQueryChange = { onAction(PaymentsListAction.OnSearchQueryChanged(it)) },
                        placeholderText = stringResource(Res.string.search_payments),
                        modifier = Modifier.fillMaxWidth()
                    )

                    PaymentsFilterStrip(
                        state = state,
                        onAction = onAction,
                        modifier = Modifier.padding(horizontal = AppDimensions.ScreenHorizontalPadding, vertical = 8.dp)
                    )
                }

                // 2. List Section
                when (state.paymentsState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            com.gaatho.rent.core.ui.components.AppExpressiveLoadingIndicator()
                        }
                    }

                    is UiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Failed to load payments", style = MaterialTheme.typography.titleMedium)
                                Button(onClick = { onAction(PaymentsListAction.OnRetry) }) {
                                    Text(stringResource(Res.string.retry))
                                }
                            }
                        }
                    }

                    is UiState.Success, is UiState.Idle -> {
                        if (state.filteredPayments.isEmpty()) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(32.dp)
                            ) {
                                EmptyStateCard(
                                    icon = Icons.Outlined.AccountBalanceWallet,
                                    title = stringResource(Res.string.no_payments_found),
                                    description = stringResource(Res.string.no_payments_found_subtitle),
                                    buttonText = stringResource(Res.string.add_payment),
                                    onButtonClick = { onAction(PaymentsListAction.OnAddPayment) }
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                itemsIndexed(
                                    items = state.filteredPayments,
                                    key = { _, payment -> payment.id },
                                    contentType = { _, _ -> "paymentRow" }
                                ) { index, payment ->
                                    PaymentRowItem(
                                        payment = payment,
                                        onClick = { onAction(PaymentsListAction.OnPaymentClicked(payment.id)) }
                                    )

                                    if (index < state.filteredPayments.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 24.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                            thickness = 0.5.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentsFilterStrip(
    state: PaymentsListState,
    onAction: (PaymentsListAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var propertyDropdownExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val options = listOf("All statuses", "Paid", "Pending", "Overdue")
        val displayOptions = listOf(stringResource(Res.string.filter_all), "Paid", "Pending", "Overdue")
        val selectedIndex = options.indexOf(state.selectedStatus).coerceAtLeast(0)

        AppSegmentedControl(
            options = displayOptions,
            selectedIndex = selectedIndex,
            onOptionSelected = { index -> 
                onAction(PaymentsListAction.OnStatusFilterChanged(options[index])) 
            },
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.width(8.dp))

        Box {
            val isPropertyFiltered = state.selectedProperty != "All properties"
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { propertyDropdownExpanded = true }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isPropertyFiltered) state.selectedProperty else stringResource(Res.string.properties_label),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isPropertyFiltered) FontWeight.Bold else FontWeight.Medium,
                        color = if (isPropertyFiltered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 100.dp)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select Property",
                    tint = if (isPropertyFiltered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            val propertyOptions = remember(state.propertiesState) {
                val props = (state.propertiesState as? UiState.Success)?.data?.map { it.name } ?: emptyList()
                listOf("All properties") + props.distinct()
            }

            DropdownMenu(
                expanded = propertyDropdownExpanded,
                onDismissRequest = { propertyDropdownExpanded = false }
            ) {
                propertyOptions.forEach { prop ->
                    DropdownMenuItem(
                        text = { Text(prop) },
                        onClick = {
                            onAction(PaymentsListAction.OnPropertyFilterChanged(prop))
                            propertyDropdownExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentRowItem(
    payment: PaymentDisplayModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Payments,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = payment.tenantName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${payment.propertyName} • ${payment.dateFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = payment.amountFormatted,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                PaymentStatusBadge(status = payment.status)
            }
        }
    }
}

@Composable
private fun PaymentStatusBadge(status: String) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "paid" -> AppColors.SuccessContainer to AppColors.Success
        "pending" -> AppColors.WarningContainer to AppColors.Warning
        "overdue" -> AppColors.ErrorContainer to AppColors.Error
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor
            )
        )
    }
}
