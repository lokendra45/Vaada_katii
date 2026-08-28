package com.gaatho.rent.features.property.presentation.details

import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.designsystem.components.RentManagerOutlinedButton
import com.gaatho.rent.core.designsystem.components.RentManagerPrimaryButton
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.ui.components.AppCard
import com.gaatho.rent.core.ui.components.*
import com.gaatho.rent.core.ui.components.AppStatusBadge
import com.gaatho.rent.core.ui.components.AppSummaryCard
import com.gaatho.rent.core.ui.components.AppTopBar
import com.gaatho.rent.core.utils.CurrencyUtil
import com.gaatho.rent.core.utils.toImageBitmap
import com.gaatho.rent.features.property.domain.model.Property
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectSideEffect
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.add_tenant
import rentmanagerapp.shared.generated.resources.edit_property_btn
import rentmanagerapp.shared.generated.resources.no_units_desc
import rentmanagerapp.shared.generated.resources.no_units_title
import rentmanagerapp.shared.generated.resources.occupied
import rentmanagerapp.shared.generated.resources.occupied_label
import rentmanagerapp.shared.generated.resources.overdue_label
import rentmanagerapp.shared.generated.resources.paid_label
import rentmanagerapp.shared.generated.resources.property_details
import rentmanagerapp.shared.generated.resources.this_month_collection
import rentmanagerapp.shared.generated.resources.total_units_label
import rentmanagerapp.shared.generated.resources.unit_assignments_title
import rentmanagerapp.shared.generated.resources.vacant_label
import rentmanagerapp.shared.generated.resources.vacant_label_short

// ─── Entry point ──────────────────────────────────────────────────────────────

@Composable
fun PropertyDetailsScreen(
    propertyId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit = {},
    onNavigateToAddTenant: () -> Unit = {},
    viewModel: PropertyDetailsViewModel = koinViewModel(parameters = { parametersOf(propertyId) })
) {
    viewModel.collectSideEffect { effect ->
        when (effect) {
            is PropertyDetailsSideEffect.NavigateBack -> onNavigateBack()
            is PropertyDetailsSideEffect.NavigateToEdit -> onNavigateToEdit(effect.propertyId)
            PropertyDetailsSideEffect.NavigateToAddTenant -> onNavigateToAddTenant()
            is PropertyDetailsSideEffect.ShowError -> {}
        }
    }

    PropertyDetailsContent(propertyId = propertyId, onAction = viewModel::onAction)
}

// ─── Content ──────────────────────────────────────────────────────────────────

@Composable
private fun PropertyDetailsContent(
    propertyId: String,
    onAction: (PropertyDetailsAction) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = stringResource(Res.string.property_details),
                onBackClick = { onAction(PropertyDetailsAction.OnBackClicked) },
                containerColor = MaterialTheme.colorScheme.background,
                titleStyle = MaterialTheme.typography.headlineMedium
            )
        },
        bottomBar = {
            PropertyDetailsActionBar(
                onEdit = { onAction(PropertyDetailsAction.OnEditClicked) },
                onAddTenant = { onAction(PropertyDetailsAction.OnAddTenantClicked) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                PropertyIdentitySection(propertyId = propertyId)

                Spacer(Modifier.height(20.dp))

                PropertyStatsSection(propertyId = propertyId)

                Spacer(Modifier.height(20.dp))

                PropertyUnitsSection(
                    propertyId = propertyId,
                    onAction = onAction
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PropertyIdentitySection(propertyId: String) {
    val viewModelStoreOwner = rememberViewModelStoreOwner()
    val viewModel = koinViewModel<PropertyIdentityViewModel>(
        viewModelStoreOwner = viewModelStoreOwner,
        parameters = { parametersOf(propertyId) }
    )
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()

    when (val s = state.propertyState) {
        is UiState.Success -> {
            val property = s.data
            PropertyHeroImage(imageUrl = property.imageUrl)

            Spacer(Modifier.height(16.dp))

            SectionTitle(
                text = property.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            BodyText(
                text = property.address,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        is UiState.Loading, UiState.Idle -> {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        is UiState.Error -> {
            Text(text = s.message, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun PropertyStatsSection(propertyId: String) {
    val viewModelStoreOwner = rememberViewModelStoreOwner()
    val viewModel = koinViewModel<PropertyStatsViewModel>(
        viewModelStoreOwner = viewModelStoreOwner,
        parameters = { parametersOf(propertyId) }
    )
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()

    val vacant = (state.totalUnits - state.occupiedUnits).coerceAtLeast(0)

    StatsRow(
        totalUnits = state.totalUnits,
        occupiedUnits = state.occupiedUnits,
        vacantUnits = vacant
    )

    Spacer(Modifier.height(20.dp))

    when (val s = state.financialState) {
        is UiState.Success -> {
            val collected = s.data.totalCollected
            val expected = state.monthlyIncome
            val percent = if (expected > 0) ((collected * 100) / expected).toInt() else 0
            CollectionSummaryCard(collected = collected, expected = expected, percent = percent)
        }
        is UiState.Loading -> {
             Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        is UiState.Error -> {}
        UiState.Idle -> {}
    }
}

@Composable
private fun PropertyUnitsSection(
    propertyId: String,
    onAction: (PropertyDetailsAction) -> Unit
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner()
    val viewModel = koinViewModel<PropertyUnitsViewModel>(
        viewModelStoreOwner = viewModelStoreOwner,
        parameters = { parametersOf(propertyId) }
    )
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()

    CardTitle(
        text = stringResource(Res.string.unit_assignments_title)
    )
    Spacer(Modifier.height(12.dp))

    when (val s = state.unitsState) {
        is UiState.Success -> {
            val assignedUnits = s.data.filter { it.paymentStatus != UnitPaymentStatus.VACANT }
            if (assignedUnits.isEmpty()) {
                UnitAssignmentsEmpty()
            } else {
                assignedUnits.forEachIndexed { index, unit ->
                    if (index > 0) Spacer(Modifier.height(12.dp))
                    TenantRow(unit = unit)
                }
            }
        }
        is UiState.Loading -> {
             Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        is UiState.Error -> {}
        UiState.Idle -> {}
    }
}

// ─── Hero image ───────────────────────────────────────────────────────────────

@Composable
private fun PropertyHeroImage(imageUrl: String?) {
    var isRendered = false
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            if (imageUrl.startsWith("base64:")) {
                val base64String = imageUrl.removePrefix("base64:")
                val bytes = try {
                    kotlin.io.encoding.Base64.Default.decode(base64String)
                } catch (e: Exception) {
                    null
                }
                val bitmap: ImageBitmap? = bytes?.toImageBitmap()
                if (bitmap != null) {
                    isRendered = true
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else if (imageUrl.startsWith("http")) {
                isRendered = true
                com.gaatho.rent.core.ui.components.AppAsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (!isRendered) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                LabelText(
                    text = stringResource(Res.string.property_no_image_added),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─── Stats ────────────────────────────────────────────────────────────────────

@Composable
private fun StatsRow(totalUnits: Int, occupiedUnits: Int, vacantUnits: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppSummaryCard(
            label = stringResource(Res.string.total_units_label),
            value = totalUnits.toString(),
            valueColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        AppSummaryCard(
            label = stringResource(Res.string.occupied),
            value = stringResource(Res.string.occupied_label, occupiedUnits),
            valueColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        AppSummaryCard(
            label = stringResource(Res.string.vacant_label_short),
            value = stringResource(Res.string.vacant_label, vacantUnits),
            valueColor = AppColors.Error,
            modifier = Modifier.weight(1f)
        )
    }
}



// ─── Collection summary ───────────────────────────────────────────────────────

@Composable
private fun CollectionSummaryCard(collected: Long, expected: Long, percent: Int) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        AppColors.EmeraldAccentDeep
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CardTitle(
                    text = stringResource(Res.string.this_month_collection)
                )
                CardTitle(
                    text = "$percent%"
                )
            }
            CardTitle(
                text = stringResource(Res.string.currency_npr) + " " + CurrencyUtil.formatNpr(collected.toDouble(), includeSymbol = false) + " / " +
                    "NPR ${CurrencyUtil.formatNpr(expected.toDouble(), includeSymbol = false)}"
            )
        }
    }
}

// ─── Unit assignments ─────────────────────────────────────────────────────────

@Composable
private fun TenantRow(unit: UnitDisplayModel) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        useCardShadow = false
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Initials avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AppColors.EmeraldAccentLight),
                contentAlignment = Alignment.Center
            ) {
                LabelText(
                    text = unit.tenantName?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                )
            }

            // Name + unit/rent
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                CardTitle(
                    text = unit.tenantName ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                CaptionText(
                    text = stringResource(Res.string.property_unit_rent_format, unit.unitNumber, CurrencyUtil.formatNpr(unit.rentPerMonth.toDouble(), includeSymbol = false)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Status badge
            val (badgeLabel, badgeBg, badgeText) = when (unit.paymentStatus) {
                UnitPaymentStatus.PAID -> Triple(
                    stringResource(Res.string.paid_label),
                    AppColors.AvatarSuccess,
                    MaterialTheme.colorScheme.primary
                )
                UnitPaymentStatus.OVERDUE -> Triple(
                    stringResource(Res.string.overdue_label),
                    AppColors.AvatarError,
                    AppColors.Error
                )
                UnitPaymentStatus.VACANT -> Triple("", Color.Transparent, Color.Transparent)
            }

            if (badgeLabel.isNotEmpty()) {
                AppStatusBadge(
                    label = badgeLabel,
                    containerColor = badgeBg,
                    contentColor = badgeText
                )
            }
        }
    }
}

@Composable
private fun UnitAssignmentsEmpty() {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        useCardShadow = false
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BodyText(
                text = stringResource(Res.string.no_units_title),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            BodySmallText(
                text = stringResource(Res.string.no_units_desc),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Action bar ───────────────────────────────────────────────────────────────

@Composable
private fun PropertyDetailsActionBar(
    onEdit: () -> Unit,
    onAddTenant: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RentManagerOutlinedButton(
            text = stringResource(Res.string.edit_property_btn),
            onClick = onEdit,
            modifier = Modifier.weight(1f)
        )

        RentManagerPrimaryButton(
            text = stringResource(Res.string.add_tenant),
            onClick = onAddTenant,
            modifier = Modifier.weight(1f)
        )
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

private val previewProperty = Property(
    id = "prop-123", ownerId = "owner-1",
    name = "Baluwatar House", address = "Baluwatar, Kathmandu, Nepal",
    imageUrl = null,
    propertyType = "RESIDENTIAL BUILDING", createdAt = "Jan 15, 2024"
)

private val previewUnits = persistentListOf(
    UnitDisplayModel("2B", "Suman Maharjan", 25_000L, UnitPaymentStatus.PAID),
    UnitDisplayModel("1A", "Anil Shrestha", 40_000L, UnitPaymentStatus.PAID),
    UnitDisplayModel("3A", "Rajesh Thapa", 18_500L, UnitPaymentStatus.OVERDUE),
)

private val previewFinancials = FinancialSummary(
    currentMonth = "August",
    totalCollected = 125_000L,
    outstandingDues = 18_500L,
)

@Preview(name = "Property Details — Loading")
@Composable
private fun PropertyDetailsLoadingPreview() {
    RentManagerTheme {
        PropertyDetailsContent(
            propertyId = "123",
            onAction = {}
        )
    }
}
