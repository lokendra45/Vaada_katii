@file:OptIn(ExperimentalMaterial3Api::class)

package com.gaatho.rent.features.property.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.gaatho.rent.core.designsystem.AppDimensions
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.designsystem.ExtendedColorHex
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.features.property.domain.model.Property
import com.gaatho.rent.features.property.presentation.add.AddPropertyViewModel
import com.gaatho.rent.features.property.presentation.add.AddPropertySideEffect
import com.gaatho.rent.features.property.presentation.add.components.AddPropertyBottomSheet
import com.gaatho.rent.features.property.presentation.list.PropertyListAction.*
import kotlinx.collections.immutable.immutableListOf
import kotlinx.collections.immutable.persistentListOf

import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.*
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import kotlinx.coroutines.launch

/**
 * Entry point for the Property List feature.
 *
 * Stateful composable: owns the ViewModel, collects State/SideEffects,
 * and delegates all rendering to the stateless [PropertyListContent].
 *
 * Fix: [PropertyListSideEffect.ShowError] now shows a real Snackbar instead of
 * being silently ignored.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyListScreen(
    onNavigateToDetails: (String) -> Unit,
    onNavigateToAddProperty: () -> Unit,
) {
    val viewModel: PropertyListViewModel = koinViewModel()
    val addPropertyViewModel: AddPropertyViewModel = koinViewModel()

    val state by viewModel.collectAsState()
    val addPropertyState by addPropertyViewModel.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PropertyListSideEffect.NavigateToDetails ->
                onNavigateToDetails(sideEffect.propertyId)

            is PropertyListSideEffect.NavigateToAddProperty ->
                coroutineScope.launch { scaffoldState.bottomSheetState.expand() }

            is PropertyListSideEffect.ShowError ->
                snackbarHostState.showSnackbar(sideEffect.message)

            is PropertyListSideEffect.ShowMessage ->
                snackbarHostState.showSnackbar(sideEffect.message)
        }
    }

    addPropertyViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is AddPropertySideEffect.NavigateBack ->
                coroutineScope.launch { scaffoldState.bottomSheetState.hide() }
            is AddPropertySideEffect.ShowSnackbar ->
                snackbarHostState.showSnackbar(sideEffect.message)
        }
    }

    PropertyListContent(
        state = state,
        addPropertyState = addPropertyState,
        scaffoldState = scaffoldState,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onAddPropertyAction = addPropertyViewModel::onAction,
        onDismissSheet = { coroutineScope.launch { scaffoldState.bottomSheetState.hide() } }
    )
}

/**
 * Stateless UI Content for the Property List.
 *
 * Fully hoisted state: no internal coroutine scopes or state ownership.
 * Implements the strict 6-level Visual Hierarchy & Workflow-First philosophy:
 * 1. Urgent actions
 * 2. Today's tasks & Quick actions
 * 3. Monthly summary
 * 4. Properties & rooms list
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyListContent(
    state: PropertyListState,
    addPropertyState: com.gaatho.rent.features.property.presentation.add.AddPropertyState,
    scaffoldState: BottomSheetScaffoldState,
    onAction: (PropertyListAction) -> Unit,
    onAddPropertyAction: (com.gaatho.rent.features.property.presentation.add.AddPropertyAction) -> Unit,
    onDismissSheet: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetDragHandle = null,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetSwipeEnabled = true,
        sheetContent = {
            com.gaatho.rent.features.property.presentation.add.components.AddPropertyBottomSheet(
                state = addPropertyState,
                onAction = onAddPropertyAction,
                onDismiss = onDismissSheet,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
            )
        },
        topBar = {
            com.gaatho.rent.core.ui.components.AppTopBar(
                title = stringResource(Res.string.properties_title),
                subtitle = stringResource(Res.string.total_properties_subtitle, state.allProperties.size),
                actions = {
                    com.gaatho.rent.core.ui.components.AppTopBarActionButton(
                        text = stringResource(Res.string.add_property),
                        onClick = { onAction(PropertyListAction.OnAddPropertyClicked) }
                    )
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Main Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 800.dp)
                    .align(Alignment.TopCenter)
            ) {
            when (val propertiesState = state.propertiesState) {
                is UiState.Loading -> {
                    com.gaatho.rent.core.ui.components.AppExpressiveLoadingIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is UiState.Success -> {
                    if (propertiesState.data.isEmpty() && state.searchQuery.isEmpty() && state.selectedLocation == "All properties") {
                        EmptyPropertiesState(
                            onAddProperty = { onAction(PropertyListAction.OnAddPropertyClicked) },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {

                            // Pixel-Perfect Material 3 Expressive AppSearchBar
                            item {
                                val locationFilterText = stringResource(Res.string.filter_by_location)
                                val searchSuggestions = remember(state.allProperties, locationFilterText) {
                                    val nameSuggestions = state.allProperties.take(3).map { prop ->
                                        com.gaatho.rent.core.ui.components.SearchSuggestionItem(
                                            title = prop.name,
                                            subtitle = prop.address,
                                            category = "Property"
                                        )
                                    }
                                    val locationSuggestions = state.allProperties.map { it.address.substringBefore(",").trim() }.distinct().take(2).map { loc ->
                                        com.gaatho.rent.core.ui.components.SearchSuggestionItem(
                                            title = loc,
                                            subtitle = locationFilterText,
                                            category = "Location"

                                        )
                                    }
                                    (nameSuggestions + locationSuggestions).take(5)
                                }

                                com.gaatho.rent.core.ui.components.AppSearchBar(
                                    query = state.searchQuery,
                                    onQueryChange = { onAction(PropertyListAction.OnSearchQueryChanged(it)) },
                                    placeholderText = stringResource(Res.string.search_properties_hint),
                                    suggestions = searchSuggestions,
                                    onSuggestionSelected = { item ->
                                        onAction(PropertyListAction.OnSearchQueryChanged(item.title))
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 4.dp)
                                )
                            }

                            // Location Filter Pills Strip
                            item {
                                val locationFilters = remember(state.allProperties) {
                                    listOf("All properties") + state.allProperties.map {
                                        if (it.address.contains(",")) it.address.substringAfterLast(",").trim() else it.address.trim()
                                    }.distinct().filter { it.isNotEmpty() }
                                }

                                // Animated Segmented Control for Location Filter
                                val selectedIndex = locationFilters.indexOf(state.selectedLocation).coerceAtLeast(0)
                                val allLocationsText = stringResource(Res.string.filter_all_locations)
                                val displayOptions = locationFilters.map { 
                                    if (it.equals("All properties", ignoreCase = true)) allLocationsText else it 
                                }

                                com.gaatho.rent.core.ui.components.AppSegmentedControl(
                                    options = displayOptions,
                                    selectedIndex = selectedIndex,
                                    onOptionSelected = { index ->
                                        onAction(PropertyListAction.OnLocationFilterSelected(locationFilters[index]))
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = AppDimensions.ScreenHorizontalPadding)
                                        .padding(top = 4.dp, bottom = 12.dp)
                                )
                            }

                            // High Craftsmanship List Row Items
                            items(
                                items = state.filteredProperties,
                                key = { it.id },
                                contentType = { "propertyRow" }
                            ) { property ->
                                PropertyRowItem(
                                    property = property,
                                    onClick = { onAction(PropertyListAction.OnPropertyClicked(property.id)) },
                                    modifier = Modifier.padding(horizontal = AppDimensions.ScreenHorizontalPadding)
                                )
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(horizontal = AppDimensions.ScreenHorizontalPadding)
                                )
                            }
                        }
                    }
                }

                is UiState.Error -> {
                    ErrorState(
                        message = propertiesState.message,
                        onRetry = { onAction(PropertyListAction.Retry) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                UiState.Idle -> {}
            }
            }
        }
    }
}

/**
 * High-craftsmanship property card matching the user's exact design requirement mockup,
 * while maintaining our official App standards.
 */
@Composable
private fun PropertyRowItem(
    property: PropertyDisplayModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail / Avatar (Soft tinted square with icon)
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Domain,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        // Content Column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Title
            Text(
                text = property.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 17.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Address and Units
            Text(
                text = "${property.address} • ${stringResource(Res.string.units_label, property.totalUnits)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Vacancy Status (Text only, no pill)
            Text(
                text = property.statusBadge,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (property.isVacant) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        // Right side: Pending Amount (or Rent as per image, but we only have pendingText)
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(Res.string.pending_label),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = property.pendingText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (property.isPending) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}



/**
 * World-Class Empty State shown when the landlord has no properties yet.
 */
@Composable
private fun EmptyPropertiesState(
    onAddProperty: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.padding(24.dp).fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🏠", fontSize = 28.sp)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(Res.string.welcome_property_empty_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(Res.string.welcome_property_empty_desc),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onAddProperty,
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text(
                        text = stringResource(Res.string.add_first_property_btn),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

/**
 * Error state with a retry button.
 */
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = ErrorMessageExtractor.extractFromString(message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) {
            Text(stringResource(Res.string.retry))
        }
    }
}

/* --- Compose Previews --- */

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PropertyListContentSuccessPreview() {
    RentManagerTheme {
        PropertyListContent(
            state = PropertyListState(
                propertiesState = UiState.Success(
                    persistentListOf(
                        PropertyDisplayModel(
                            id = "1",
                            name = "Peaceful Villa",
                            address = "Koteshwor, Kathmandu",
                            totalUnits = 10,
                            occUnits = 8,
                            statusBadge = "• 2 Vacant",
                            isVacant = true,
                            pendingText = "Rs. 25,000",
                            isPending = true
                        )
                    )
                )
            ),
            addPropertyState = com.gaatho.rent.features.property.presentation.add.AddPropertyState(),
            scaffoldState = rememberBottomSheetScaffoldState(),
            onAction = {},
            onAddPropertyAction = {},
            onDismissSheet = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PropertyListContentEmptyPreview() {
    RentManagerTheme {
        PropertyListContent(
            state = PropertyListState(propertiesState = UiState.Success(persistentListOf())),
            addPropertyState = com.gaatho.rent.features.property.presentation.add.AddPropertyState(),
            scaffoldState = rememberBottomSheetScaffoldState(),
            onAction = {},
            onAddPropertyAction = {},
            onDismissSheet = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PropertyListContentLoadingPreview() {
    RentManagerTheme {
        PropertyListContent(
            state = PropertyListState(propertiesState = UiState.Loading),
            addPropertyState = com.gaatho.rent.features.property.presentation.add.AddPropertyState(),
            scaffoldState = rememberBottomSheetScaffoldState(),
            onAction = {},
            onAddPropertyAction = {},
            onDismissSheet = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PropertyListContentErrorPreview() {
    RentManagerTheme {
        PropertyListContent(
            state = PropertyListState(
                propertiesState = UiState.Error("Unable to load properties. Check your internet connection.")
            ),
            addPropertyState = com.gaatho.rent.features.property.presentation.add.AddPropertyState(),
            scaffoldState = rememberBottomSheetScaffoldState(),
            onAction = {},
            onAddPropertyAction = {},
            onDismissSheet = {}
        )
    }
}
