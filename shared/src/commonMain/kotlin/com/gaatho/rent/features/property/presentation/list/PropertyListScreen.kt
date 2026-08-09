@file:OptIn(ExperimentalMaterial3Api::class)

package com.gaatho.rent.features.property.presentation.list

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Business
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
import androidx.compose.ui.layout.ContentScale
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
import com.gaatho.rent.core.utils.toImageBitmap
import com.gaatho.rent.core.ui.components.AppDialog
import com.gaatho.rent.features.property.presentation.list.PropertyListAction.*
import io.github.vinceglb.filekit.readBytes
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
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PropertyListSideEffect.NavigateToDetails ->
                onNavigateToDetails(sideEffect.propertyId)

            is PropertyListSideEffect.NavigateToAddProperty ->
                showBottomSheet = true

            is PropertyListSideEffect.ShowError ->
                snackbarHostState.showSnackbar(sideEffect.message)

            is PropertyListSideEffect.ShowMessage ->
                snackbarHostState.showSnackbar(sideEffect.message)
        }
    }

    addPropertyViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is AddPropertySideEffect.NavigateBack ->
                showBottomSheet = false
            is AddPropertySideEffect.ShowSuccessDialog -> {
                showBottomSheet = false
                showSuccessDialog = true
            }
            is AddPropertySideEffect.ShowSnackbar ->
                snackbarHostState.showSnackbar(sideEffect.message)
        }
    }

    val imagePicker = io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher(
        type = io.github.vinceglb.filekit.dialogs.FileKitType.Image,
    ) { file ->
        coroutineScope.launch {
            val bytes = file?.readBytes()
            addPropertyViewModel.onAction(com.gaatho.rent.features.property.presentation.add.AddPropertyAction.OnImagePicked(bytes))
        }
    }

    PropertyListContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onAddPropertyAction = addPropertyViewModel::onAction
    )

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            contentWindowInsets = { WindowInsets.ime }
        ) {
            com.gaatho.rent.features.property.presentation.add.components.AddPropertyBottomSheet(
                state = addPropertyState,
                onAction = addPropertyViewModel::onAction,
                onDismiss = { showBottomSheet = false },
                onPickImage = { imagePicker.launch() },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
            )
        }
    }

    if (showSuccessDialog) {
        AppDialog(
            icon = Icons.Default.CheckCircle,
            variant = AppDialog.Variant.Success,
            title = stringResource(Res.string.success),
            body = stringResource(Res.string.property_created_success_body),
            confirmText = stringResource(Res.string.continue_btn),
            onConfirm = { showSuccessDialog = false },
            onDismiss = { showSuccessDialog = false },
            dismissText = null
        )
    }
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
    onAction: (PropertyListAction) -> Unit,
    onAddPropertyAction: (com.gaatho.rent.features.property.presentation.add.AddPropertyAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val filterOptions = persistentListOf("All Properties", "Active", "Pending Dues", "Vacant")
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 800.dp)
                    .align(Alignment.CenterHorizontally)
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

                                    // Scrollable Filter Chips for Location Filter
                                    LazyRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = AppDimensions.ScreenHorizontalPadding)
                                            .padding(top = 4.dp, bottom = 12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(locationFilters) { location ->
                                            val displayOption = if (location.equals("All properties", ignoreCase = true)) stringResource(Res.string.filter_all_locations) else location
                                            FilterChip(
                                                selected = state.selectedLocation == location,
                                                onClick = { onAction(PropertyListAction.OnLocationFilterSelected(location)) },
                                                label = { Text(displayOption) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            )
                                        }
                                    }
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
@OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)
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
        // Thumbnail / Avatar (Image or Soft tinted square with initials)
        val imageUrl = property.imageUrl
        var isImageRendered = false
        
        if (imageUrl != null && imageUrl.startsWith("base64:")) {
            val base64String = imageUrl.removePrefix("base64:")
            val bytes = try {
                kotlin.io.encoding.Base64.Default.decode(base64String)
            } catch (e: Exception) {
                null
            }
            val bitmap = bytes?.toImageBitmap()
            if (bitmap != null) {
                isImageRendered = true
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
        
        if (!isImageRendered) {
            val initials = com.gaatho.rent.core.utils.TenantUtils.getInitials(property.name)
            val avatarColors = com.gaatho.rent.core.utils.TenantUtils.getAvatarColors(property.name)
            
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(avatarColors.first)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(avatarColors.second),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Content Column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Title
            Text(
                text = property.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Address and Units
            Text(
                text = "${property.address} • ${stringResource(Res.string.units_label, property.totalUnits)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Vacancy Status (Text only, no pill)
            Text(
                text = property.statusBadge,
                style = MaterialTheme.typography.labelSmall,
                color = if (property.isVacant) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Right side: Quick status
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            val isErrorState = property.isPending
            Surface(
                shape = RoundedCornerShape(AppDimensions.RadiusPill),
                color = if (isErrorState) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = property.pendingText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isErrorState) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
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
    Box(modifier = modifier.padding(24.dp).fillMaxWidth()) {
        com.gaatho.rent.core.ui.components.EmptyStateCard(
            icon = Icons.Outlined.Business,
            title = stringResource(Res.string.welcome_property_empty_title),
            description = stringResource(Res.string.welcome_property_empty_desc),
            buttonText = stringResource(Res.string.add_first_property_btn),
            onButtonClick = onAddProperty
        )
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
            style = MaterialTheme.typography.bodyMedium,
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
                            name = "Sunrise Apartments",
                            address = "Baneshwor, Kathmandu",
                            imageUrl = null,
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
            state = PropertyListState(propertiesState = UiState.Success(persistentListOf())),
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
            onAction = {},
            onAddPropertyAction = {}
        )
    }
}
