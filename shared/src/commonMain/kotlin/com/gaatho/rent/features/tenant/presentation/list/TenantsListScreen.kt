package com.gaatho.rent.features.tenant.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.gaatho.rent.core.designsystem.AppDimensions
import com.gaatho.rent.core.designsystem.ExtendedColorHex
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.ui.components.AppSearchBar
import com.gaatho.rent.features.tenant.domain.model.Tenant
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.*
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import com.gaatho.rent.core.ui.components.AppSegmentedControl

@Composable
fun TenantsListScreen(
    onNavigateToDetails: (String) -> Unit,
    onNavigateToAddTenant: () -> Unit
) {
    val viewModel: TenantsListViewModel = koinViewModel()
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is TenantsListSideEffect.NavigateToTenantDetails ->
                onNavigateToDetails(sideEffect.tenantId)
            is TenantsListSideEffect.ShowError ->
                snackbarHostState.showSnackbar(sideEffect.message)
            is TenantsListSideEffect.ShowMessage ->
                snackbarHostState.showSnackbar(sideEffect.message)
        }
    }

    TenantsListContent(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantsListContent(
    state: TenantsListState,
    onAction: (TenantsListAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            com.gaatho.rent.core.ui.components.AppTopBar(
                title = stringResource(Res.string.tenants_title),
                subtitle = "${state.totalCount} total · ${state.activeCount} active",
                actions = {
                    com.gaatho.rent.core.ui.components.AppTopBarActionButton(
                        text = stringResource(Res.string.add_tenant),
                        onClick = { showBottomSheet = true }
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 800.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                // 1. Search & Filter Section with clean layout
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pixel-Perfect Material 3 Search Bar (with vertical Results & < Back arrow on focus)
                    val searchSuggestions = remember(state.allTenants) {
                        val tenantSuggestions = state.allTenants.take(3).map { tenant ->
                            com.gaatho.rent.core.ui.components.SearchSuggestionItem(
                                title = tenant.name,
                                subtitle = tenant.propertyName ?: "Assigned Room",
                                category = "Tenant"
                            )
                        }
                        val propertySuggestions = state.allTenants.mapNotNull { it.propertyName }.distinct().take(2).map { prop ->
                            com.gaatho.rent.core.ui.components.SearchSuggestionItem(
                                title = prop,
                                subtitle = "Filter by Property",
                                category = "Property"
                            )
                        }
                        (tenantSuggestions + propertySuggestions).take(5)
                    }

                    AppSearchBar(
                        query = state.searchQuery,
                        onQueryChange = { onAction(TenantsListAction.OnSearchQueryChanged(it)) },
                        placeholderText = stringResource(Res.string.search_tenants),
                        suggestions = searchSuggestions,
                        onSuggestionSelected = { item ->
                            onAction(TenantsListAction.OnSearchQueryChanged(item.title))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Extracted to prevent entire screen recomposition when dropdowns toggle
                    TenantsFilterStrip(
                        state = state,
                        onAction = onAction,
                        modifier = Modifier.padding(horizontal = AppDimensions.ScreenHorizontalPadding, vertical = 8.dp)
                    )
                }

                // 2. List Section without outer card container (clean edge-to-edge native rows)
                when (state.tenantsState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            com.gaatho.rent.core.ui.components.AppExpressiveLoadingIndicator()
                        }
                    }

                    is UiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(stringResource(Res.string.tenant_failed_load), style = MaterialTheme.typography.titleMedium)
                                Button(onClick = { onAction(TenantsListAction.OnRetry) }) {
                                    Text(stringResource(Res.string.retry))
                                }
                            }
                        }
                    }

                    is UiState.Success, is UiState.Idle -> {
                        if (state.filteredTenants.isEmpty()) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(32.dp)
                            ) {
                                com.gaatho.rent.core.ui.components.EmptyStateCard(
                                    icon = Icons.Outlined.Group,
                                    title = stringResource(Res.string.no_tenants_found),
                                    description = stringResource(Res.string.no_tenants_found_subtitle),
                                    buttonText = "Add Tenant",
                                    onButtonClick = { showBottomSheet = true }
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
                                    items = state.filteredTenants,
                                    key = { _, tenant -> tenant.id },
                                    contentType = { _, _ -> "tenantRow" }
                                ) { index, tenant ->
                                    TenantRowItem(
                                        tenant = tenant,
                                        onClick = { onAction(TenantsListAction.OnTenantClicked(tenant.id)) }
                                    )

                                    if (index < state.filteredTenants.lastIndex) {
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

        } // Column
    } // Scaffold content

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            contentWindowInsets = { WindowInsets.ime }
        ) {
            com.gaatho.rent.features.tenant.presentation.list.components.AddTenantBottomSheet(
                onDismiss = { showBottomSheet = false },
                onSave = { showBottomSheet = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
            )
        }
    }
} // TenantsListContent

@Composable
private fun TenantsFilterStrip(
    state: TenantsListState,
    onAction: (TenantsListAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var propertyDropdownExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Segmented Control for Status
        val options = listOf("All statuses", "Active", "Inactive")
        val displayOptions = listOf(stringResource(Res.string.filter_all), stringResource(Res.string.filter_active), stringResource(Res.string.filter_past))
        val selectedIndex = options.indexOf(state.selectedStatus).coerceAtLeast(0)

        AppSegmentedControl(
            options = displayOptions,
            selectedIndex = selectedIndex,
            onOptionSelected = { index -> 
                onAction(TenantsListAction.OnStatusFilterChanged(options[index])) 
            },
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.width(8.dp))

        // Minimal Property Filter
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

            val propertyOptions = remember(state.allTenants) {
                listOf("All properties") + state.allTenants.mapNotNull { it.propertyName }.distinct()
            }

            DropdownMenu(
                expanded = propertyDropdownExpanded,
                onDismissRequest = { propertyDropdownExpanded = false }
            ) {
                propertyOptions.forEach { prop ->
                    DropdownMenuItem(
                        text = { Text(prop) },
                        onClick = {
                            onAction(TenantsListAction.OnPropertyFilterChanged(prop))
                            propertyDropdownExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TenantRowItem(
    tenant: TenantDisplayModel,
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
            // Perfect Circle Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(tenant.avatarBgColorHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tenant.initials,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(tenant.avatarTextColorHex),
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Main Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = tenant.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = tenant.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Trailing Side
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusBadge(status = tenant.status, isActive = tenant.isActive)
                
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "View Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String, isActive: Boolean) {
    val bgColor = if (isActive) Color(ExtendedColorHex.ActiveBackground) else Color(ExtendedColorHex.InactiveBackground)
    val textColor = if (isActive) Color(ExtendedColorHex.ActiveText) else Color(ExtendedColorHex.InactiveText)

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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun TenantsListScreenPreview() {
    com.gaatho.rent.core.designsystem.RentManagerTheme {
        val dummyTenants = kotlinx.collections.immutable.persistentListOf(
            TenantDisplayModel(
                id = "1",
                name = "Brooklyn Simmons",
                initials = "BS",
                subtitle = "Sunrise Residency • Room 4A",
                status = "Active",
                isActive = true,
                avatarBgColorHex = 0xFFE3F2FD,
                avatarTextColorHex = 0xFF1976D2,
                propertyName = "Sunrise Residency",
                roomNumber = "Room 4A",
                email = null,
                phone = null
            ),
            TenantDisplayModel(
                id = "2",
                name = "Darlene Robertson",
                initials = "DR",
                subtitle = "Ganga Nivas • Room 5",
                status = "Inactive",
                isActive = false,
                avatarBgColorHex = 0xFFFBE9E7,
                avatarTextColorHex = 0xFFD32F2F,
                propertyName = "Ganga Nivas",
                roomNumber = "Room 5",
                email = null,
                phone = null
            ),
            TenantDisplayModel(
                id = "3",
                name = "Marvin McKinney",
                initials = "MM",
                subtitle = "Sunrise Residency • Room 1B",
                status = "Active",
                isActive = true,
                avatarBgColorHex = 0xFFE8F5E9,
                avatarTextColorHex = 0xFF388E3C,
                propertyName = "Sunrise Residency",
                roomNumber = "Room 1B",
                email = null,
                phone = null
            )
        )
        val dummyState = TenantsListState(
            tenantsState = com.gaatho.rent.core.ui.UiState.Success(dummyTenants),
            filteredTenants = dummyTenants,
            selectedStatus = "All statuses",
            selectedProperty = "All properties"
        )
        TenantsListContent(state = dummyState, onAction = {})
    }
}

