package com.gaatho.rent.features.tenant.presentation.list

import com.gaatho.rent.core.ui.components.*

import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.AppDimensions
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.ui.components.AppBadge
import com.gaatho.rent.core.ui.components.AppBadgeType
import com.gaatho.rent.core.ui.components.AppCard
import com.gaatho.rent.core.ui.components.AppSearchBar
import com.gaatho.rent.core.utils.CurrencyUtil
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectSideEffect
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.retry
import rentmanagerapp.shared.generated.resources.tenant_failed_load

@Composable
fun TenantsListScreen(
    onNavigateToDetails: (String) -> Unit,
    onNavigateToAddTenant: () -> Unit,
    onNavigateBack: (() -> Unit)? = null
) {
    val viewModel: TenantsListViewModel = koinViewModel()
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    val searchText by viewModel.searchText.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val pagedTenants = viewModel.pagedTenantsFlow.collectAsLazyPagingItems()
    val coroutineScope = rememberCoroutineScope()
    var archivedPromptData by remember { mutableStateOf<TenantsListSideEffect.ShowArchivedPrompt?>(null) }
    var isSavingBackup by remember { mutableStateOf(false) }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is TenantsListSideEffect.NavigateToTenantDetails ->
                onNavigateToDetails(sideEffect.tenantId)
            is TenantsListSideEffect.ShowError ->
                snackbarHostState.showSnackbar(sideEffect.message)
            is TenantsListSideEffect.ShowMessage ->
                snackbarHostState.showSnackbar(sideEffect.message)
            is TenantsListSideEffect.ShowArchivedPrompt ->
                archivedPromptData = sideEffect
        }
    }

    // Refresh pager each time this screen is composed (e.g. after navigating back)
    LaunchedEffect(Unit) {
        pagedTenants.refresh()
    }

    if (archivedPromptData != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { /* Force action */ },
            title = { Text(stringResource(Res.string.tenant_archived_backup_title)) },
            text = { Text(stringResource(Res.string.tenant_archived_prompt_body, archivedPromptData!!.tenantName)) },
            confirmButton = {
                Button(
                    onClick = {
                        val promptData = archivedPromptData!!
                        isSavingBackup = true
                        coroutineScope.launch {
                            val bytes = com.gaatho.rent.core.utils.generateTenantPdf(
                                tenantName = promptData.tenantName,
                                profileInfo = promptData.profileInfo,
                                rentInfo = promptData.rentInfo
                            )
                            val success = com.gaatho.rent.core.utils.savePdfFile(bytes, "${promptData.tenantName.replace(" ", "_")}_Backup.pdf")
                            isSavingBackup = false
                            if (success) {
                                snackbarHostState.showSnackbar("Backup saved to Downloads/Documents.")
                                viewModel.onAction(TenantsListAction.OnArchivedTenantBackupCompleted(promptData.tenantId))
                                archivedPromptData = null
                            } else {
                                snackbarHostState.showSnackbar("Failed to save backup.")
                            }
                        }
                    },
                    enabled = !isSavingBackup
                ) {
                    if (isSavingBackup) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(stringResource(Res.string.tenant_backup_delete_button))
                    }
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { archivedPromptData = null }) {
                    Text(stringResource(Res.string.cancel_action))
                }
            }
        )
    }

    TenantsListContent(
        state = state,
        searchText = searchText,
        isSearching = isSearching,
        pagedTenants = pagedTenants,
        onNavigateToAddTenant = onNavigateToAddTenant,
        onNavigateBack = onNavigateBack,
        onAction = viewModel::onAction,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantsListContent(
    state: TenantsListState,
    searchText: String,
    isSearching: Boolean,
    pagedTenants: LazyPagingItems<TenantDisplayModel>,
    onNavigateToAddTenant: () -> Unit,
    onAction: (TenantsListAction) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    onSearchQueryChanged: (String) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {

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
        topBar = {
            com.gaatho.rent.core.ui.components.AppTopBar(
                title = stringResource(Res.string.tenant_list_title),
                onBackClick = onNavigateBack,
                containerColor = MaterialTheme.colorScheme.background
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            AnimatedVisibility(
                visible = isFabVisible,
                enter = slideInVertically(initialOffsetY = { it * 2 }),
                exit = slideOutVertically(targetOffsetY = { it * 2 })
            ) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToAddTenant,
                    shape = RoundedCornerShape(50),
                    containerColor = AppColors.EmeraldAccent,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add Tenant") },
                    text = { Text(stringResource(Res.string.tenant_add_title)) },
                    expanded = true
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 800.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val searchSuggestions = remember { emptyList<com.gaatho.rent.core.ui.components.SearchSuggestionItem>() }

                    AppSearchBar(
                        query = searchText,
                        onQueryChange = onSearchQueryChanged,
                        placeholderText = "Search tenants...",
                        suggestions = searchSuggestions,
                        onSuggestionSelected = { item ->
                            onSearchQueryChanged(item.title)
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    )

                    TenantsFilterStrip(
                        state = state,
                        pagedTenants = pagedTenants,
                        onAction = onAction,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (isSearching) {
                    TenantSkeletonLoadingState()
                } else {
                    com.gaatho.rent.core.ui.components.AppAnimatedState(
                        targetState = pagedTenants.loadState.refresh,
                        modifier = Modifier.fillMaxSize()
                    ) { refreshState ->
                        when (refreshState) {
                            is LoadState.Loading -> {
                                TenantSkeletonLoadingState()
                            }
                            is LoadState.Error -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CardTitle(stringResource(Res.string.tenant_failed_load))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(onClick = { pagedTenants.retry() }) {
                                            Text(stringResource(Res.string.retry))
                                        }
                                    }
                                }
                            }
                            is LoadState.NotLoading -> {
                                if (pagedTenants.itemCount == 0) {
                                    if (state.debouncedQuery.isNotEmpty() || state.selectedStatus != "All statuses" || state.selectedProperty != "All properties") {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxWidth().weight(1f).padding(32.dp)
                                        ) {
                                            Text(stringResource(Res.string.tenant_no_match_criteria), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    } else {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                                .padding(32.dp)
                                        ) {
                                            com.gaatho.rent.core.ui.components.AppIllustratedEmptyState(
                                                icon = Icons.Default.Person,
                                                title = stringResource(Res.string.tenant_no_tenants_title),
                                                description = "Add your first tenant to start tracking rent"
                                            )
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(
                                            start = 20.dp,
                                            end = 20.dp,
                                            top = 8.dp,
                                            bottom = 100.dp
                                        ),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                                    onClick = { onAction(TenantsListAction.OnTenantClicked(tenant.id)) },
                                                    modifier = Modifier.animateItem()
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
                }
            }
        }
    }
}

@Composable
private fun TenantsFilterStrip(
    state: TenantsListState,
    pagedTenants: LazyPagingItems<TenantDisplayModel>,
    onAction: (TenantsListAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("All statuses" to "All", "Active" to "Active", "Inactive" to "Inactive", "Pending" to "Pending")

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { (value, label) ->
            val selected = state.selectedStatus == value
            val displayLabel = if (value == "All statuses") {
                "All (${pagedTenants.itemCount})"
            } else label

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(
                        if (selected) AppColors.EmeraldAccent else MaterialTheme.colorScheme.surface
                    )
                    .clickable { onAction(TenantsListAction.OnStatusFilterChanged(value)) }
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                LabelText(
                    text = displayLabel
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
    AppCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        useCardShadow = false,
        containerColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            // --- Top Row: Avatar & Name/Status & Quick Actions ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(tenant.avatarBgColorHex)),
                    contentAlignment = Alignment.Center
                ) {
                    LabelText(
                        text = tenant.initials
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Status
                Column(modifier = Modifier.weight(1f)) {
                    CardTitle(
                        text = tenant.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AppBadge(
                        text = tenant.status,
                        type = if (tenant.isActive) AppBadgeType.SUCCESS 
                               else if (tenant.status.equals("Pending", ignoreCase = true)) AppBadgeType.WARNING 
                               else AppBadgeType.NEUTRAL
                    )
                }

                // Quick Actions (Icon Buttons)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "View Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Bottom Section: Property & Rent Info ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Property Info
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        CaptionText(
                            text = stringResource(Res.string.property_details_title),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (tenant.propertyName.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable { /* TODO: Trigger assign action */ }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Assign",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    MicroText(
                                        text = stringResource(Res.string.tenant_assign_button),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        } else {
                            LabelText(
                                text = tenant.propertyName,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!tenant.roomNumber.isNullOrBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    CaptionText(
                                        text = stringResource(Res.string.tenant_unit_format, tenant.roomNumber),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Rent Info
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        CaptionText(
                            text = stringResource(Res.string.tenant_rent_label),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        AmountText(
                            text = CurrencyUtil.formatNprLabel(tenant.rentAmount),
                            color = MaterialTheme.colorScheme.primary
                        )
                        CaptionText(
                            text = stringResource(Res.string.tenant_per_month_suffix),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun TenantsListScreenPreview() {
    RentManagerTheme {
        val dummyTenants = persistentListOf(
            TenantDisplayModel(
                id = "1", name = "Suman Maharjan", initials = "SM",
                subtitle = "Sundar Niwas • Unit 2A", status = "Active", isActive = true,
                avatarBgColorHex = 0xFFE2DFFF, avatarTextColorHex = 0xFF3323CC,
                propertyName = "Sundar Niwas", roomNumber = "Unit 2A",
                email = null, phone = null, rentAmount = 25000
            ),
            TenantDisplayModel(
                id = "2", name = "Anita Shrestha", initials = "AS",
                subtitle = "Krishna Bhawan • Unit 1B", status = "Active", isActive = true,
                avatarBgColorHex = 0xFFF0FDF4, avatarTextColorHex = 0xFF15803D,
                propertyName = "Krishna Bhawan", roomNumber = "Unit 1B",
                email = null, phone = null, rentAmount = 18000
            ),
            TenantDisplayModel(
                id = "3", name = "Bikash Thapa", initials = "BT",
                subtitle = "Baluwatar House • Unit 3C", status = "Pending", isActive = false,
                avatarBgColorHex = 0xFFE0F2FE, avatarTextColorHex = 0xFF0369A1,
                propertyName = "Baluwatar House", roomNumber = "Unit 3C",
                email = null, phone = null, rentAmount = 30000
            )
        )
        val dummyState = TenantsListState(
            selectedStatus = "All statuses",
            selectedProperty = "All properties",
        )
        val pagedTenants = flowOf(PagingData.from(dummyTenants)).collectAsLazyPagingItems()
        TenantsListContent(
            state = dummyState,
            searchText = "",
            isSearching = false,
            pagedTenants = pagedTenants,
            onNavigateToAddTenant = {},
            onAction = {},
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
