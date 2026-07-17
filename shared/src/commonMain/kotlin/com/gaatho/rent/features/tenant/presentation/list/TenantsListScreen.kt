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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.gaatho.rent.core.designsystem.AppDimensions
import com.gaatho.rent.core.designsystem.ExtendedColorHex
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.core.ui.components.AppSearchBar
import com.gaatho.rent.features.tenant.domain.model.Tenant
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

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
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var propertyDropdownExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    // Calculate blur and scrim opacity based on sheet state
    val isSheetVisible = scaffoldState.bottomSheetState.targetValue != SheetValue.Hidden
    val blurRadius by animateDpAsState(
        targetValue = if (isSheetVisible) AppDimensions.RadiusMedium else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )
    val scrimAlpha by animateFloatAsState(
        targetValue = if (isSheetVisible) 0.45f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetDragHandle = null,
        sheetContainerColor = Color.Transparent, // Let the bottom sheet content handle its own bg
        sheetContent = {
            com.gaatho.rent.features.tenant.presentation.list.components.AddTenantBottomSheet(
                onDismiss = { coroutineScope.launch { scaffoldState.bottomSheetState.hide() } },
                onSave = { coroutineScope.launch { scaffoldState.bottomSheetState.hide() } },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            )
        },
        topBar = {
            com.gaatho.rent.core.ui.components.AppTopBar(
                title = "Tenants",
                subtitle = "${state.totalCount} total · ${state.activeCount} active",
                actions = {
                    com.gaatho.rent.core.ui.components.AppTopBarActionButton(
                        text = "Add tenant",
                        onClick = { coroutineScope.launch { scaffoldState.bottomSheetState.expand() } }
                    )
                },
                modifier = Modifier.blur(blurRadius)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Main Content with Blur
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 800.dp)
                    .align(Alignment.TopCenter)
                    .blur(blurRadius)
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
                        placeholderText = "Search name, email, phone or property",
                        suggestions = searchSuggestions,
                        onSuggestionSelected = { item ->
                            onAction(TenantsListAction.OnSearchQueryChanged(item.title))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Properly aligned Dropdown Filter Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status Filter Dropdown Pill
                        Box {
                            val isStatusFiltered = state.selectedStatus != "All statuses"
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isStatusFiltered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
                                ),
                                color = if (isStatusFiltered) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .height(36.dp)
                                    .clickable { statusDropdownExpanded = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = state.selectedStatus,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (isStatusFiltered) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isStatusFiltered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Select Status",
                                        tint = if (isStatusFiltered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = statusDropdownExpanded,
                                onDismissRequest = { statusDropdownExpanded = false }
                            ) {
                                listOf("All statuses", "Active", "Inactive").forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(status) },
                                        onClick = {
                                            onAction(TenantsListAction.OnStatusFilterChanged(status))
                                            statusDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Property Filter Dropdown Pill
                        Box {
                            val isPropertyFiltered = state.selectedProperty != "All properties"
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isPropertyFiltered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
                                ),
                                color = if (isPropertyFiltered) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .height(36.dp)
                                    .clickable { propertyDropdownExpanded = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = state.selectedProperty,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (isPropertyFiltered) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isPropertyFiltered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Select Property",
                                        tint = if (isPropertyFiltered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
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
                                Text("Failed to load tenants", style = MaterialTheme.typography.titleMedium)
                                Button(onClick = { onAction(TenantsListAction.OnRetry) }) {
                                    Text("Retry")
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
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "No tenants found",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "Try adjusting your search or filter settings.",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
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
                                    key = { _, tenant -> tenant.id }
                                ) { index, tenant ->
                                    TenantRowItem(
                                        tenant = tenant,
                                        onClick = { onAction(TenantsListAction.OnTenantClicked(tenant.id)) }
                                    )

                                    if (index < state.filteredTenants.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 20.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Scrim Overlay
            if (scrimAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = scrimAlpha))
                        .clickable(enabled = false) {} // Consume clicks
                )
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
        color = Color.Transparent
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(tenant.avatarBgColorHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tenant.initials,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(tenant.avatarTextColorHex)
                    )
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Middle details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = tenant.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Status Pill
                    StatusBadge(status = tenant.status, isActive = tenant.isActive)
                }

                Text(
                    text = tenant.subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
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


