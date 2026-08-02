package com.gaatho.rent.features.property.presentation.details

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.ui.components.AppDialog
import com.gaatho.rent.features.property.domain.model.Property
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

// ─── Entry point ──────────────────────────────────────────────────────────────

@Composable
fun PropertyDetailsScreen(
    propertyId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit = {},
    onNavigateToTenantDetails: (String) -> Unit = {},
    viewModel: PropertyDetailsViewModel = koinInject(parameters = { parametersOf(propertyId) })
) {
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is PropertyDetailsSideEffect.NavigateBack -> onNavigateBack()
            is PropertyDetailsSideEffect.NavigateToEdit -> onNavigateToEdit(effect.propertyId)
            is PropertyDetailsSideEffect.ShowError -> {}
        }
    }

    PropertyDetailsContent(state = state, onAction = viewModel::onAction)
}

// ─── Content ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PropertyDetailsContent(
    state: PropertyDetailsState,
    onAction: (PropertyDetailsAction) -> Unit,
) {
    val scrollState = rememberScrollState()

    // Delete confirmation dialog
    if (state.showDeleteConfirm) {
        AppDialog(
            variant     = AppDialog.Variant.Destructive,
            layout      = AppDialog.Layout.Center,
            icon        = Icons.Default.Delete,
            title       = "Delete Property?",
            body        = "This will permanently delete this property and all associated data. This cannot be undone.",
            confirmText = "Delete",
            dismissText = "Cancel",
            onConfirm   = { onAction(PropertyDetailsAction.OnDeleteConfirmed) },
            onDismiss   = { onAction(PropertyDetailsAction.OnDeleteDismissed) },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Property Details",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(PropertyDetailsAction.OnBackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onAction(PropertyDetailsAction.OnEditClicked) }
                    ) {
                        Text(
                            text = "Edit",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            when (val propState = state.propertyState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }

                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = propState.message, color = MaterialTheme.colorScheme.error)
                    }
                }

                is UiState.Success -> {
                    val property = propState.data

                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Spacer(Modifier.height(8.dp))

                        // 1. Property Identity
                        PropertyIdentitySection(property = property)

                        // 2. Key Metrics (Monthly Income + Occupancy)
                        KeyMetricsSection(
                            monthlyIncome = state.monthlyIncome,
                            occupiedUnits = state.occupiedUnits,
                            totalUnits = state.totalUnits
                        )

                        // 3. Property Overview card
                        PropertyOverviewCard(property = property, totalUnits = state.totalUnits)

                        // 4. Financial Summary card
                        when (val fs = state.financialState) {
                            is UiState.Success -> FinancialSummaryCard(summary = fs.data)
                            else -> {}
                        }

                        // 5. Units list
                        when (val us = state.unitsState) {
                            is UiState.Success ->
                                UnitsSection(
                                    units = us.data,
                                    onUnitClick = { onAction(PropertyDetailsAction.OnUnitClicked(it)) },
                                    onViewAll = { onAction(PropertyDetailsAction.OnViewAllUnitsClicked) }
                                )
                            else -> {}
                        }

                        Spacer(Modifier.height(32.dp))
                    }
                }

                UiState.Idle -> {}
            }
        }
    }
}

// ─── 1. Property Identity ─────────────────────────────────────────────────────

@Composable
private fun PropertyIdentitySection(property: Property) {
    Column {
        Text(
            text = property.name,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = property.address,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─── 2. Key Metrics ───────────────────────────────────────────────────────────

@Composable
private fun KeyMetricsSection(
    monthlyIncome: Long,
    occupiedUnits: Int,
    totalUnits: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricCard(
            modifier = Modifier.weight(1f),
            label = "Monthly Income"
        ) {
            Text(
                text = "NPR",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = formatNpr(monthlyIncome),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
        MetricCard(
            modifier = Modifier.weight(1f),
            label = "Occupancy"
        ) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = occupiedUnits.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "/$totalUnits",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Text(
                text = "Units",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

// ─── 3. Property Overview ─────────────────────────────────────────────────────

@Composable
private fun PropertyOverviewCard(property: Property, totalUnits: Int) {
    SectionCard(title = "Property Overview") {
        OverviewRow(label = "Property Type", value = property.propertyType.replace("_", " ").titleCase())
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        OverviewRow(label = "Total Units", value = if (totalUnits > 0) totalUnits.toString() else "—")
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        OverviewRow(
            label = "Added On",
            value = property.createdAt ?: "—"
        )
    }
}

@Composable
private fun OverviewRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ─── 4. Financial Summary ─────────────────────────────────────────────────────

@Composable
private fun FinancialSummaryCard(summary: FinancialSummary) {
    // Pulsing alpha for outstanding amount
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Column {
            // Header row with month badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Financial Summary",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = summary.currentMonth.uppercase().take(4),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Total collected
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(Color(0xFFDCFCE7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        text = "Total Collected",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "NPR ${formatNpr(summary.totalCollected)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Outstanding dues
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Outstanding Dues",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "NPR ${formatNpr(summary.outstandingDues)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error.copy(alpha = if (summary.outstandingDues > 0) pulseAlpha else 1f)
                )
            }
        }
    }
}

// ─── 5. Units List ────────────────────────────────────────────────────────────

@Composable
private fun UnitsSection(
    units: ImmutableList<UnitDisplayModel>,
    onUnitClick: (String) -> Unit,
    onViewAll: () -> Unit,
) {
    Column {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Units",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = onViewAll) {
                Text(
                    text = "View All",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        if (units.isEmpty()) {
            EmptyUnitsCard()
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 1.dp
            ) {
                Column {
                    units.forEachIndexed { index, unit ->
                        UnitRow(unit = unit, onClick = { onUnitClick(unit.unitNumber) })
                        if (index < units.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnitRow(unit: UnitDisplayModel, onClick: () -> Unit) {
    val isVacant = unit.paymentStatus == UnitPaymentStatus.VACANT

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Unit number badge
        if (isVacant) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unit.unitNumber,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unit.unitNumber,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Name + rent
        Column(modifier = Modifier.weight(1f)) {
            if (unit.tenantName != null) {
                Text(
                    text = unit.tenantName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = "Vacant",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Italic
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "NPR ${formatNpr(unit.rentPerMonth)}/mo",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Status chip
        UnitStatusChip(status = unit.paymentStatus)

        // Chevron
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun UnitStatusChip(status: UnitPaymentStatus) {
    val (label, bgColor, textColor, borderColor) = when (status) {
        UnitPaymentStatus.PAID -> ChipStyle(
            "Paid",
            Color(0xFFF0FFF4), Color(0xFF15803D), Color(0xFFDCFCE7)
        )
        UnitPaymentStatus.OVERDUE -> ChipStyle(
            "Overdue",
            Color(0xFFFFF5F5), Color(0xFFDC2626), Color(0xFFFEE2E2)
        )
        UnitPaymentStatus.VACANT -> ChipStyle(
            "Vacant",
            Color(0xFFF3F4F6), Color(0xFF6B7280), Color(0xFFE5E7EB)
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(100.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp,
                letterSpacing = 0.2.sp
            ),
            color = textColor
        )
    }
}

private data class ChipStyle(
    val label: String,
    val bg: Color,
    val text: Color,
    val border: Color
)

@Composable
private fun EmptyUnitsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.DoorBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "No units yet",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Add tenants from the Tenants tab to populate units.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Shared section card ──────────────────────────────────────────────────────

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 1.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun formatNpr(amount: Long): String {
    if (amount == 0L) return "0"
    // Nepali-style: 1,00,000
    val s = amount.toString()
    if (s.length <= 3) return s
    val last3 = s.takeLast(3)
    val rest = s.dropLast(3)
    val groups = mutableListOf<String>()
    var idx = rest.length
    while (idx > 0) {
        val start = maxOf(0, idx - 2)
        groups.add(0, rest.substring(start, idx))
        idx = start
    }
    return groups.joinToString(",") + ",$last3"
}

private fun String.titleCase() =
    split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }

// ─── Previews ─────────────────────────────────────────────────────────────────

private val previewProperty = Property(
    id = "prop-123", ownerId = "owner-1",
    name = "Ward 3, Jhamsikhel", address = "Jhamsikhel, Lalitpur",
    propertyType = "RESIDENTIAL BUILDING", createdAt = "Jan 15, 2024"
)

private val previewUnits = persistentListOf(
    UnitDisplayModel("1A", "Ramesh Sharma", 15_000L, UnitPaymentStatus.PAID),
    UnitDisplayModel("1B", "Sita Magar", 15_000L, UnitPaymentStatus.OVERDUE),
    UnitDisplayModel("2A", "Hari Thapa", 20_000L, UnitPaymentStatus.PAID),
    UnitDisplayModel("2B", null, 20_000L, UnitPaymentStatus.VACANT),
)

private val previewFinancials = FinancialSummary(
    currentMonth = "August",
    totalCollected = 115_000L,
    outstandingDues = 30_000L,
)

@Preview(name = "Property Details — Success")
@Composable
private fun PropertyDetailsSuccessPreview() {
    RentManagerTheme {
        PropertyDetailsContent(
            state = PropertyDetailsState(
                propertyState = UiState.Success(previewProperty),
                unitsState = UiState.Success(previewUnits),
                financialState = UiState.Success(previewFinancials),
                monthlyIncome = 145_000L,
                occupiedUnits = 8,
                totalUnits = 10,
            ),
            onAction = {}
        )
    }
}

@Preview(name = "Property Details — Loading")
@Composable
private fun PropertyDetailsLoadingPreview() {
    RentManagerTheme {
        PropertyDetailsContent(
            state = PropertyDetailsState(),
            onAction = {}
        )
    }
}

@Preview(name = "Property Details — Empty Units")
@Composable
private fun PropertyDetailsEmptyPreview() {
    RentManagerTheme {
        PropertyDetailsContent(
            state = PropertyDetailsState(
                propertyState = UiState.Success(previewProperty),
                unitsState = UiState.Success(persistentListOf()),
                financialState = UiState.Success(FinancialSummary("August", 0L, 0L)),
                monthlyIncome = 0L, occupiedUnits = 0, totalUnits = 0,
            ),
            onAction = {}
        )
    }
}

@Preview(name = "Property Details — Delete Dialog")
@Composable
private fun PropertyDetailsDeletePreview() {
    RentManagerTheme {
        PropertyDetailsContent(
            state = PropertyDetailsState(
                propertyState = UiState.Success(previewProperty),
                unitsState = UiState.Success(previewUnits),
                financialState = UiState.Success(previewFinancials),
                monthlyIncome = 145_000L, occupiedUnits = 8, totalUnits = 10,
                showDeleteConfirm = true,
            ),
            onAction = {}
        )
    }
}
