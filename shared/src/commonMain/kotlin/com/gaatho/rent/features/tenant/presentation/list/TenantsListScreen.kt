package com.gaatho.rent.features.tenant.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import androidx.paging.LoadState
import androidx.paging.PagingData
import kotlinx.coroutines.flow.flowOf
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
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.designsystem.ExtendedColorHex
import com.gaatho.rent.core.designsystem.Spacing
import com.gaatho.rent.core.designsystem.Radius
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gaatho.rent.core.ui.components.AppSegmentedControl
// Removed AddTenant imports

@Composable
fun TenantsListScreen(
    onNavigateToDetails: (String) -> Unit,
    onNavigateToAddTenant: () -> Unit
) {
    val viewModel: TenantsListViewModel = koinViewModel()
    val state by viewModel.collectAsState()
    // Collect the search query directly from the ViewModel's StateFlow.
    // NiA pattern: search text never touches Orbit state.
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
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
    val pagedTenants = viewModel.pagedTenantsFlow.collectAsLazyPagingItems()

    TenantsListContent(
        state = state,
        searchQuery = searchQuery,
        pagedTenants = pagedTenants,
        onNavigateToAddTenant = onNavigateToAddTenant,
        onAction = viewModel::onAction,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantsListContent(
    state: TenantsListState,
    searchQuery: String = "",
    pagedTenants: LazyPagingItems<TenantDisplayModel>,
    onNavigateToAddTenant: () -> Unit,
    onAction: (TenantsListAction) -> Unit,
    onSearchQueryChanged: (String) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {

    Scaffold(
        topBar = {
            com.gaatho.rent.core.ui.components.AppTopBar(
                title = stringResource(Res.string.tenants_title),
                subtitle = "${pagedTenants.itemCount} loaded",
                actions = {
                    com.gaatho.rent.core.ui.components.AppTopBarActionButton(
                        text = stringResource(Res.string.add_tenant),
                        onClick = onNavigateToAddTenant
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
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
                    val searchSuggestions = remember { emptyList<com.gaatho.rent.core.ui.components.SearchSuggestionItem>() }

                    AppSearchBar(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChanged,
                        placeholderText = stringResource(Res.string.search_tenants),
                        suggestions = searchSuggestions,
                        onSuggestionSelected = { item ->
                            onSearchQueryChanged(item.title)
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
                when (pagedTenants.loadState.refresh) {
                    is LoadState.Loading -> {
                        TenantSkeletonLoadingState()
                    }

                    is LoadState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(stringResource(Res.string.tenant_failed_load), style = MaterialTheme.typography.titleMedium)
                                Button(onClick = { pagedTenants.retry() }) {
                                    Text(stringResource(Res.string.retry))
                                }
                            }
                        }
                    }

                    is LoadState.NotLoading -> {
                        if (pagedTenants.itemCount == 0) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(32.dp)
                            ) {
                                com.gaatho.rent.core.ui.components.AppIllustratedEmptyState(
                                    illustration = Res.drawable.empty_tenants,
                                    title = stringResource(Res.string.no_tenants_found),
                                    description = stringResource(Res.string.no_tenants_found_subtitle),
                                    buttonText = "Add Tenant",
                                    onButtonClick = onNavigateToAddTenant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = Spacing.ScreenPadding,
                                    end = Spacing.ScreenPadding,
                                    top = Spacing.Scale8,
                                    bottom = 100.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(Spacing.ItemGap)
                            ) {
                                items(
                                    count = pagedTenants.itemCount,
                                    key = pagedTenants.itemKey { it.id },
                                    contentType = pagedTenants.itemContentType { "tenantRow" }
                                ) { index ->
                                    val tenant = pagedTenants[index]
                                    if (tenant != null) {
                                        TenantRowItem(
                                            tenant = tenant,
                                            onClick = { onAction(TenantsListAction.OnTenantClicked(tenant.id)) }
                                        )
                                    }
                                }
                                
                                if (pagedTenants.loadState.append is LoadState.Loading) {
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

        } // Column
    } // Scaffold content
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

            val propertyOptions = remember(state.propertiesState) {
                listOf("All properties") + ((state.propertiesState as? UiState.Success)?.data?.map { it.name } ?: emptyList())
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
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent, // Figma list style (transparent background, uses outer surface)
        shape = RoundedCornerShape(Radius.Md),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
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
            selectedStatus = "All statuses",
            selectedProperty = "All properties"
        )
        val pagedTenants = flowOf(PagingData.from(dummyTenants)).collectAsLazyPagingItems()
        TenantsListContent(
            state = dummyState, 
            pagedTenants = pagedTenants, 
            onNavigateToAddTenant = {},
            onAction = {}
        )
    }
}

@Composable
fun TenantSkeletonLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = AppDimensions.ScreenHorizontalPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        repeat(5) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                com.gaatho.rent.core.ui.components.AppShimmerBox(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.gaatho.rent.core.ui.components.AppShimmerBox(modifier = Modifier.fillMaxWidth(0.5f).height(18.dp))
                    com.gaatho.rent.core.ui.components.AppShimmerBox(modifier = Modifier.fillMaxWidth(0.3f).height(14.dp))
                }
                com.gaatho.rent.core.ui.components.AppShimmerBox(
                    modifier = Modifier.width(70.dp).height(24.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}
