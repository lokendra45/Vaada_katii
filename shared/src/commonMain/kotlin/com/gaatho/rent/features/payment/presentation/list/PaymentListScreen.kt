package com.gaatho.rent.features.payment.presentation.list

import com.gaatho.rent.core.ui.components.*

import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.ui.components.AppBadge
import com.gaatho.rent.core.ui.components.AppBadgeType
import com.gaatho.rent.core.ui.components.AppListItemSurface
import com.gaatho.rent.core.ui.components.AppSearchBar
import com.gaatho.rent.core.utils.CurrencyUtil
import com.gaatho.rent.core.utils.TenantUtils
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.add_payment
import rentmanagerapp.shared.generated.resources.filter_all_months
import rentmanagerapp.shared.generated.resources.no_payments_found
import rentmanagerapp.shared.generated.resources.no_payments_found_subtitle
import rentmanagerapp.shared.generated.resources.payment_filter_last_month
import rentmanagerapp.shared.generated.resources.payment_filter_this_month
import rentmanagerapp.shared.generated.resources.payment_method_bank
import rentmanagerapp.shared.generated.resources.payment_method_cash
import rentmanagerapp.shared.generated.resources.payment_method_esewa
import rentmanagerapp.shared.generated.resources.payment_method_khalti
import rentmanagerapp.shared.generated.resources.payments_title
import rentmanagerapp.shared.generated.resources.pending_label
import rentmanagerapp.shared.generated.resources.retry
import rentmanagerapp.shared.generated.resources.collected_label

@Composable
fun PaymentListScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPaymentDetails: (String) -> Unit,
    onNavigateToAddPayment: () -> Unit = {},
    viewModel: PaymentListViewModel = koinViewModel()
) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    val searchText by viewModel.searchText.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val pagedPayments = viewModel.pagedPaymentsFlow.collectAsLazyPagingItems()

    // Refresh pager each time this screen is composed (e.g. after navigating back)
    LaunchedEffect(Unit) {
        pagedPayments.refresh()
    }

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

    PaymentListContent(
        state = state,
        searchText = searchText,
        isSearching = isSearching,
        pagedPayments = pagedPayments,
        onNavigateToAddPayment = onNavigateToAddPayment,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentListContent(
    state: PaymentListState,
    searchText: String,
    isSearching: Boolean,
    pagedPayments: LazyPagingItems<PaymentDisplayModel>,
    onNavigateToAddPayment: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onAction: (PaymentListAction) -> Unit
) {
    val today = remember {
        kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    val listState = rememberLazyListState()
    var isFabVisible by remember { mutableStateOf(true) }

    LaunchedEffect(listState) {
        var previousIndex = listState.firstVisibleItemIndex
        var previousScrollOffset = listState.firstVisibleItemScrollOffset

        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                if (index > previousIndex || (index == previousIndex && offset > previousScrollOffset + 10)) {
                    isFabVisible = false
                } else if (index < previousIndex || (index == previousIndex && offset < previousScrollOffset - 10)) {
                    isFabVisible = true
                }
                previousIndex = index
                previousScrollOffset = offset
            }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            AnimatedVisibility(
                visible = isFabVisible,
                enter = slideInVertically(initialOffsetY = { it * 2 }),
                exit = slideOutVertically(targetOffsetY = { it * 2 })
            ) {
                ExtendedFloatingActionButton(
                    onClick = { onAction(PaymentListAction.OnAddPaymentClicked) },
                    shape = RoundedCornerShape(50),
                    containerColor = AppColors.EmeraldAccent,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    text = { Text(stringResource(Res.string.add_payment)) },
                    expanded = true
                )
            }
        }
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
                // Header — title + month filter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle(
                        text = stringResource(Res.string.payments_title),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Search
                AppSearchBar(
                    query = searchText,
                    onQueryChange = onSearchQueryChanged,
                    placeholderText = stringResource(Res.string.payment_search_placeholder),
                    suggestions = emptyList(),
                    onSuggestionSelected = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )

                val filterOptions = listOf(
                    PaymentListFilters.ThisMonth,
                    PaymentListFilters.LastMonth,
                    PaymentListFilters.AllMonths
                )
                val displayLabels = listOf(
                    stringResource(Res.string.payment_filter_this_month),
                    stringResource(Res.string.payment_filter_last_month),
                    stringResource(Res.string.filter_all_months)
                )
                val selectedIndex = filterOptions.indexOf(state.selectedMonth).coerceAtLeast(0)

                com.gaatho.rent.core.ui.components.AppFilterChips(
                    options = displayLabels,
                    selectedIndex = selectedIndex,
                    onOptionSelected = { index ->
                        onAction(PaymentListAction.OnMonthFilterChanged(filterOptions[index]))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                )

                Spacer(Modifier.height(4.dp))

                if (isSearching) {
                    PaymentSkeletonLoadingState()
                } else {
                    // Paged content with LoadState handling
                    when (pagedPayments.loadState.refresh) {
                        is LoadState.Loading -> {
                            PaymentSkeletonLoadingState()
                        }
                        is LoadState.Error -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Failed to load payments",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Button(onClick = { pagedPayments.retry() }) {
                                        Text(stringResource(Res.string.retry))
                                    }
                                }
                            }
                        }
                        is LoadState.NotLoading -> {
                            if (pagedPayments.itemCount == 0) {
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    com.gaatho.rent.core.ui.components.AppIllustratedEmptyState(
                                        icon = Icons.Default.AttachMoney,
                                        title = stringResource(Res.string.no_payments_found),
                                        description = stringResource(Res.string.no_payments_found_subtitle)
                                    )
                                }
                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(
                                        top = 8.dp,
                                        bottom = 100.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    // Group items by date for section headers
                                    var lastDate: String? = null
                                    for (index in 0 until pagedPayments.itemCount) {
                                        val payment = pagedPayments.peek(index)
                                        val date = payment?.date
                                        if (date != null && date != lastDate) {
                                            lastDate = date
                                            val headerDate = date
                                            item(key = "header_$headerDate") {
                                                AppSectionHeader(
                                                    title = paymentGroupHeader(headerDate, today)
                                                )
                                            }
                                        }
                                        item(
                                            key = payment?.id ?: "item_$index",
                                            contentType = "paymentRow"
                                        ) {
                                            val p = pagedPayments[index]
                                            if (p != null) {
                                                PaymentRowItem(
                                                    payment = p,
                                                    onPaymentClicked = { id ->
                                                        onAction(PaymentListAction.OnPaymentClicked(id))
                                                    },
                                                    modifier = Modifier.animateItem()
                                                )
                                            }
                                        }
                                    }

                                    // Append loading indicator
                                    if (pagedPayments.loadState.append is LoadState.Loading) {
                                        item {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
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
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentRowItem(
    payment: PaymentDisplayModel,
    onPaymentClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val initials = TenantUtils.getInitials(payment.tenantName)
    val subtitle = buildString {
        append(payment.propertyName)
        if (payment.unit != null) append(" - Unit ${payment.unit}")
    }
    Column(modifier = modifier) {
        AppListItemSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            onClick = { onPaymentClicked(payment.id) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    LabelText(
                        text = initials,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                CardTitle(
                    text = payment.tenantName,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                BodySmallText(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                AmountText(
                    text = payment.formattedAmount,
                    color = AppColors.EmeraldAccent
                )
                Spacer(Modifier.height(4.dp))
                PaymentMethodBadge(payment.paymentMethod)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp).align(Alignment.CenterVertically)
            )
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}
}

@Composable
private fun PaymentMethodBadge(method: String?) {
    val normalized = method?.uppercase() ?: ""
    val isCash = normalized == "CASH"
    val label = when (normalized) {
        "CASH" -> stringResource(Res.string.payment_method_cash)
        "BANK_TRANSFER" -> stringResource(Res.string.payment_method_bank)
        "ESEWA" -> stringResource(Res.string.payment_method_esewa)
        "KHALTI" -> stringResource(Res.string.payment_method_khalti)
        else -> normalized.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    }
    AppBadge(
        text = label,
        type = if (isCash) AppBadgeType.WARNING else AppBadgeType.INFO
    )
}

private val monthNames = arrayOf(
    "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
    "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
)

private fun paymentGroupHeader(dateIso: String, today: LocalDate): String {
    val date = try {
        LocalDate.parse(dateIso.substring(0, 10))
    } catch (e: Exception) {
        return dateIso.uppercase()
    }
    val dayMonth = "${monthNames[date.month.ordinal]} ${date.day}"
    return when (date) {
        today -> "TODAY ($dayMonth)"
        today.minus(1, DateTimeUnit.DAY) -> "YESTERDAY ($dayMonth)"
        else -> {
            val day = if (date.day < 10) "0${date.day}" else date.day.toString()
            "$day ${monthNames[date.month.ordinal]} ${date.year}"
        }
    }
}

private fun currentMonthLabel(): String {
    val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val shortMonths = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    return "${shortMonths[now.month.ordinal]} ${now.year}"
}

@Composable
fun PaymentSkeletonLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(5) {
            Row(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                com.gaatho.rent.core.ui.components.AppShimmerBox(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.gaatho.rent.core.ui.components.AppShimmerBox(modifier = Modifier.fillMaxWidth(0.5f).height(14.dp))
                    com.gaatho.rent.core.ui.components.AppShimmerBox(modifier = Modifier.fillMaxWidth(0.3f).height(10.dp))
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.gaatho.rent.core.ui.components.AppShimmerBox(modifier = Modifier.width(60.dp).height(14.dp))
                    com.gaatho.rent.core.ui.components.AppShimmerBox(modifier = Modifier.width(40.dp).height(14.dp), shape = RoundedCornerShape(12.dp))
                }
            }
        }
    }
}
