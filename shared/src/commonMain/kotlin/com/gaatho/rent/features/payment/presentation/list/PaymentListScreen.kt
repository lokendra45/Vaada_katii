package com.gaatho.rent.features.payment.presentation.list

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.ui.components.AppSearchBar
import com.gaatho.rent.core.ui.components.AppStatusBadge
import com.gaatho.rent.core.utils.CurrencyUtil
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.add_payment
import rentmanagerapp.shared.generated.resources.collected_label
import rentmanagerapp.shared.generated.resources.empty_payments
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
import rentmanagerapp.shared.generated.resources.search_payments

@Composable
fun PaymentListScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPaymentDetails: (String) -> Unit,
    onNavigateToAddPayment: () -> Unit = {},
    viewModel: PaymentListViewModel = koinInject()
) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

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
        searchQuery = searchQuery,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentListContent(
    state: PaymentListState,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onAction: (PaymentListAction) -> Unit
) {
    val payments = (state.paymentsState as? UiState.Success)?.data ?: persistentListOf<PaymentDisplayModel>()
    val collected = payments.filter { it.isPaid }.sumOf { it.amount }
    val pending = payments.filter { !it.isPaid }.sumOf { it.amount }
    val today = remember { kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAction(PaymentListAction.OnAddPaymentClicked) },
                shape = RoundedCornerShape(50),
                containerColor = AppColors.EmeraldAccent,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(Res.string.add_payment)) }
            )
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
                    Text(
                        text = stringResource(Res.string.payments_title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    MonthFilterPill(
                        selectedMonth = state.selectedMonth,
                        onMonthSelected = { onAction(PaymentListAction.OnMonthFilterChanged(it)) }
                    )
                }

                // Summary — collected / pending
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryBox(
                        label = stringResource(Res.string.collected_label),
                        value = "NPR ${CurrencyUtil.formatNpr(collected.toDouble(), includeSymbol = false)}",
                        accent = AppColors.EmeraldAccent,
                        background = Color(0xFFECFDF5),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryBox(
                        label = stringResource(Res.string.pending_label),
                        value = "NPR ${CurrencyUtil.formatNpr(pending.toDouble(), includeSymbol = false)}",
                        accent = Color(0xFFDB354F),
                        background = Color(0xFFFFF3F5),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Search
                AppSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChanged,
                    placeholderText = stringResource(Res.string.search_payments),
                    suggestions = emptyList(),
                    onSuggestionSelected = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )

                Spacer(Modifier.height(4.dp))

                when (val s = state.paymentsState) {
                    is UiState.Loading, UiState.Idle -> {
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
                                    Text(stringResource(Res.string.retry))
                                }
                            }
                        }
                    }
                    is UiState.Success -> {
                        if (s.data.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                                com.gaatho.rent.core.ui.components.AppIllustratedEmptyState(
                                    illustration = Res.drawable.empty_payments,
                                    title = stringResource(Res.string.no_payments_found),
                                    description = stringResource(Res.string.no_payments_found_subtitle),
                                    buttonText = stringResource(Res.string.add_payment),
                                    onButtonClick = { onAction(PaymentListAction.OnAddPaymentClicked) }
                                )
                            }
                        } else {
                            val groups = buildGroups(s.data)
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 24.dp,
                                    end = 24.dp,
                                    top = 8.dp,
                                    bottom = 96.dp
                                )
                            ) {
                                groups.forEach { group ->
                                    item(key = "header_${group.date}") {
                                        SectionHeader(
                                            text = paymentGroupHeader(group.date, today)
                                        )
                                    }
                                    items(group.items, key = { it.id }) { payment ->
                                        PaymentRowItem(
                                            payment = payment,
                                            onClick = { onAction(PaymentListAction.OnPaymentClicked(payment.id)) }
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
private fun SummaryBox(
    label: String,
    value: String,
    accent: Color,
    background: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(57.dp),
        shape = RoundedCornerShape(10.dp),
        color = background
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = accent
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = accent
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthFilterPill(
    selectedMonth: String,
    onMonthSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        PaymentListFilters.ThisMonth,
        PaymentListFilters.LastMonth,
        PaymentListFilters.AllMonths
    )
    val label = when (selectedMonth) {
        PaymentListFilters.ThisMonth -> stringResource(Res.string.payment_filter_this_month)
        PaymentListFilters.LastMonth -> stringResource(Res.string.payment_filter_last_month)
        else -> currentMonthLabel()
    }

    Box {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                val optionLabel = when (option) {
                    PaymentListFilters.ThisMonth -> stringResource(Res.string.payment_filter_this_month)
                    PaymentListFilters.LastMonth -> stringResource(Res.string.payment_filter_last_month)
                    else -> stringResource(Res.string.filter_all_months)
                }
                DropdownMenuItem(
                    text = { Text(optionLabel) },
                    onClick = {
                        onMonthSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentRowItem(
    payment: PaymentDisplayModel,
    onClick: () -> Unit
) {
    val initials = payment.tenantName.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")
    val subtitle = buildString {
        append(payment.propertyName)
        if (payment.unit != null) append(" - Unit ${payment.unit}")
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = AppColors.EmeraldAccent
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = payment.tenantName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "NPR ${CurrencyUtil.formatNpr(payment.amount.toDouble(), includeSymbol = false)}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    ),
                    color = AppColors.EmeraldAccent
                )
                Spacer(Modifier.height(4.dp))
                PaymentMethodBadge(payment.paymentMethod)
            }
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
    AppStatusBadge(
        label = label,
        containerColor = if (isCash) Color(0xFFFFF3F5) else AppColors.EmeraldAccentLight,
        contentColor = if (isCash) AppColors.Error else AppColors.EmeraldAccent,
        fontSize = 9.sp,
        verticalPadding = 2.dp
    )
}
@Immutable
private data class PaymentGroup(
    val date: String,
    val items: List<PaymentDisplayModel>
)

private fun buildGroups(
    payments: ImmutableList<PaymentDisplayModel>
): ImmutableList<PaymentGroup> {
    if (payments.isEmpty()) return persistentListOf()

    val groups = mutableListOf<PaymentGroup>()
    var currentDate = payments.first().date
    var bucket = mutableListOf<PaymentDisplayModel>()

    for (payment in payments) {
        if (payment.date != currentDate) {
            groups += PaymentGroup(currentDate, bucket.toImmutableList())
            currentDate = payment.date
            bucket = mutableListOf()
        }
        bucket += payment
    }
    groups += PaymentGroup(currentDate, bucket.toImmutableList())

    return groups.toImmutableList()
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
