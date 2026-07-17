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
import androidx.compose.material.icons.filled.Home
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

import org.koin.compose.viewmodel.koinViewModel
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
    
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    // Orbit's collectSideEffect uses LaunchedEffect + repeatOnLifecycle(STARTED) internally.
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PropertyListSideEffect.NavigateToDetails ->
                onNavigateToDetails(sideEffect.propertyId)

            is PropertyListSideEffect.NavigateToAddProperty -> {
                coroutineScope.launch {
                    bottomSheetScaffoldState.bottomSheetState.expand()
                }
            }

            is PropertyListSideEffect.ShowError ->
                snackbarHostState.showSnackbar(sideEffect.message)

            is PropertyListSideEffect.ShowMessage ->
                snackbarHostState.showSnackbar(sideEffect.message)
        }
    }

    addPropertyViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is AddPropertySideEffect.NavigateBack -> {
                coroutineScope.launch {
                    bottomSheetScaffoldState.bottomSheetState.hide()
                }
            }
            is AddPropertySideEffect.ShowSnackbar -> {
                snackbarHostState.showSnackbar(sideEffect.message)
            }
        }
    }

    // Calculate blur and scrim opacity based on sheet state
    val isSheetVisible = bottomSheetScaffoldState.bottomSheetState.targetValue != SheetValue.Hidden
    val blurRadius by animateDpAsState(
        targetValue = if (isSheetVisible) AppDimensions.RadiusMedium else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )
    val scrimAlpha by animateFloatAsState(
        targetValue = if (isSheetVisible) 0.45f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    PropertyListContent(
        state = state,
        addPropertyState = addPropertyState,
        scaffoldState = bottomSheetScaffoldState,
        snackbarHostState = snackbarHostState,
        blurRadius = blurRadius,
        scrimAlpha = scrimAlpha,
        onAction = viewModel::onAction,
        onAddPropertyAction = addPropertyViewModel::onAction
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
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    blurRadius: androidx.compose.ui.unit.Dp = 0.dp,
    scrimAlpha: Float = 0f
) {
    val coroutineScope = rememberCoroutineScope()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetDragHandle = null,
        sheetContainerColor = Color.Transparent,
        sheetContent = {
            AddPropertyBottomSheet(
                state = addPropertyState,
                onAction = onAddPropertyAction,
                onDismiss = { coroutineScope.launch { scaffoldState.bottomSheetState.hide() } },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            )
        },
        topBar = {
            com.gaatho.rent.core.ui.components.AppTopBar(
                title = "Properties",
                subtitle = "${state.allProperties.size} total properties",
                actions = {
                    com.gaatho.rent.core.ui.components.AppTopBarActionButton(
                        text = "Add property",
                        onClick = { onAction(OnAddPropertyClicked) }
                    )
                },
                modifier = Modifier.blur(blurRadius)
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Main Content with Blur
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 800.dp)
                    .align(Alignment.TopCenter)
                    .blur(blurRadius)
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
                                val searchSuggestions = remember(state.allProperties) {
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
                                            subtitle = "Filter by Location",
                                            category = "Location"
                                        )
                                    }
                                    (nameSuggestions + locationSuggestions).take(5)
                                }

                                com.gaatho.rent.core.ui.components.AppSearchBar(
                                    query = state.searchQuery,
                                    onQueryChange = { onAction(PropertyListAction.OnSearchQueryChanged(it)) },
                                    placeholderText = "Search properties by name or location...",
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

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.PaddingSmall),
                                    contentPadding = PaddingValues(horizontal = AppDimensions.ScreenHorizontalPadding),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp, bottom = 12.dp)
                                ) {
                                    items(locationFilters) { location ->
                                        val isSelected = state.selectedLocation == location
                                        Surface(
                                            shape = RoundedCornerShape(AppDimensions.RadiusPill),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                            ),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(AppDimensions.RadiusPill))
                                                .clickable {
                                                    onAction(PropertyListAction.OnLocationFilterSelected(location))
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                if (!location.equals("All properties", ignoreCase = true)) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.LocationOn,
                                                        contentDescription = null,
                                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                                Text(
                                                    text = if (location.equals("All properties", ignoreCase = true)) "All Properties" else location,
                                                    style = MaterialTheme.typography.labelLarge.copy(
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // High Craftsmanship Requirement Property Cards
                            items(
                                items = state.filteredProperties,
                                key = { it.id }
                            ) { property ->
                                RequirementPropertyCard(
                                    property = property,
                                    onClick = { onAction(PropertyListAction.OnPropertyClicked(property.id)) },
                                    modifier = Modifier.padding(horizontal = AppDimensions.ScreenHorizontalPadding, vertical = 6.dp)
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

/**
 * High-craftsmanship property card matching the user's exact design requirement mockup,
 * while maintaining our official App standards.
 */
@Composable
private fun RequirementPropertyCard(
    property: PropertyDisplayModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Row: Title + Address on left, Status Badge on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = property.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = property.address,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Status Pill Badge (• 2 Vacant / • Fully Occupied)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (property.isVacant) Color(ExtendedColorHex.VacantBackground) else Color(ExtendedColorHex.OccupiedBackground),
                    border = BorderStroke(1.dp, if (property.isVacant) Color(ExtendedColorHex.VacantBorder) else Color(ExtendedColorHex.OccupiedBorder))
                ) {
                    Text(
                        text = property.statusBadge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (property.isVacant) Color(ExtendedColorHex.VacantText) else Color(ExtendedColorHex.OccupiedText)
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Subtle divider line
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 1.dp
            )

            // Bottom Row: UNITS column & PENDING column
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: UNITS
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "UNITS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    )
                    Text(
                        text = "${property.totalUnits} Total (${property.occUnits} Occ.)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                // Right: PENDING
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "PENDING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    )
                    Text(
                        text = property.pendingText,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = if (property.isPending) MaterialTheme.colorScheme.onSurface else Color(ExtendedColorHex.ActiveText)
                        )
                    )
                }
            }
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
                    text = "Welcome to Rent Manager Nepal 🙏",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Your daily landlord productivity center. Add your first house, flat, or room to start recording rent and managing tenants.",
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
                        text = "Add Your First Property",
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
            Text("Retry")
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
                    listOf(
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
            onAddPropertyAction = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PropertyListContentEmptyPreview() {
    RentManagerTheme {
        PropertyListContent(
            state = PropertyListState(propertiesState = UiState.Success(emptyList())),
            addPropertyState = com.gaatho.rent.features.property.presentation.add.AddPropertyState(),
            scaffoldState = rememberBottomSheetScaffoldState(),
            onAction = {},
            onAddPropertyAction = {}
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
            onAddPropertyAction = {}
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
            onAddPropertyAction = {}
        )
    }
}
