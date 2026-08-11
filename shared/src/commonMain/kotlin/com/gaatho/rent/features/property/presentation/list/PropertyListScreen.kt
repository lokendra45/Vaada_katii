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
// Removed AddPropertyBottomSheet imports
import com.gaatho.rent.core.utils.toImageBitmap
import com.gaatho.rent.core.ui.components.AppDialog
import com.gaatho.rent.features.property.presentation.list.PropertyListAction.*
import io.github.vinceglb.filekit.readBytes
import kotlinx.collections.immutable.immutableListOf
import kotlinx.collections.immutable.persistentListOf
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.LoadState

import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.*
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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

    val state by viewModel.collectAsState()
    // Collect the search query directly from the ViewModel's StateFlow.
    // This is the NiA pattern: the search text never touches Orbit state.
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PropertyListSideEffect.NavigateToDetails ->
                onNavigateToDetails(sideEffect.propertyId)

            is PropertyListSideEffect.NavigateToAddProperty ->
                onNavigateToAddProperty()

            is PropertyListSideEffect.ShowError ->
                snackbarHostState.showSnackbar(sideEffect.message)
            is PropertyListSideEffect.ShowMessage ->
                snackbarHostState.showSnackbar(sideEffect.message)
        }
    }
    val pagedProperties = viewModel.pagedPropertiesFlow.collectAsLazyPagingItems()

    PropertyListContent(
        state = state,
        searchQuery = searchQuery,
        pagedProperties = pagedProperties,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onNavigateToAddProperty = onNavigateToAddProperty
    )
}

/**
 * Stateless UI Content for the Property List.
 *
 * Fully hoisted state: no internal coroutine scopes or state ownership.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyListContent(
    state: PropertyListState,
    searchQuery: String = "",
    pagedProperties: LazyPagingItems<PropertyDisplayModel>? = null,
    onAction: (PropertyListAction) -> Unit,
    onSearchQueryChanged: (String) -> Unit = {},
    onNavigateToAddProperty: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            com.gaatho.rent.core.ui.components.AppTopBar(
                title = stringResource(Res.string.properties_title),
                subtitle = stringResource(Res.string.total_properties_subtitle, pagedProperties?.itemCount ?: 0),
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 800.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                if (pagedProperties == null) {
                    PropertySkeletonLoadingState()
                    return@Box
                }

                val refreshState = pagedProperties.loadState.refresh
                val appendState = pagedProperties.loadState.append
                val isEmpty = pagedProperties.itemCount == 0

                if (refreshState is LoadState.Loading) {
                    PropertySkeletonLoadingState()
                } else if (refreshState is LoadState.Error) {
                    ErrorState(
                        message = refreshState.error.message ?: "Failed to load",
                        onRetry = { pagedProperties.retry() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (isEmpty && searchQuery.isEmpty() && state.selectedLocation == "All properties") {
                    EmptyPropertiesState(
                        onAddProperty = { onAction(PropertyListAction.OnAddPropertyClicked) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            com.gaatho.rent.core.ui.components.AppSearchBar(
                                query = searchQuery,
                                onQueryChange = onSearchQueryChanged,
                                placeholderText = stringResource(Res.string.search_properties_hint),
                                suggestions = emptyList(),
                                onSuggestionSelected = {},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                            )
                        }

                        item {
                            // Simplified hardcoded filters for demo
                            val locationFilters = listOf("All properties", "Kathmandu", "Lalitpur", "Bhaktapur")

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

                        items(count = pagedProperties.itemCount) { index ->
                            val property = pagedProperties[index]
                            if (property != null) {
                                PropertyRowItem(
                                    property = property,
                                    onClick = { onAction(PropertyListAction.OnPropertyClicked(property.id)) },
                                    modifier = Modifier.padding(horizontal = AppDimensions.ScreenHorizontalPadding)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                        
                        if (appendState is LoadState.Loading) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
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
                        .clip(CircleShape)
                )
            }
        }
        
        if (!isImageRendered) {
            val initials = com.gaatho.rent.core.utils.TenantUtils.getInitials(property.name)
            val avatarColors = com.gaatho.rent.core.utils.TenantUtils.getAvatarColors(property.name)
            
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
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
        com.gaatho.rent.core.ui.components.AppIllustratedEmptyState(
            illustration = Res.drawable.empty_properties,
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
            state = PropertyListState(),
            onAction = {},
            onNavigateToAddProperty = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PropertyListContentEmptyPreview() {
    RentManagerTheme {
        PropertyListContent(
            state = PropertyListState(),
            onAction = {},
            onNavigateToAddProperty = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PropertyListContentLoadingPreview() {
    RentManagerTheme {
        PropertyListContent(
            state = PropertyListState(),
            onAction = {},
            onNavigateToAddProperty = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PropertyListContentErrorPreview() {
    RentManagerTheme {
        PropertyListContent(
            state = PropertyListState(),
            onAction = {},
            onNavigateToAddProperty = {}
        )
    }
}

@Composable
fun PropertySkeletonLoadingState(modifier: Modifier = Modifier) {
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
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.gaatho.rent.core.ui.components.AppShimmerBox(modifier = Modifier.fillMaxWidth(0.6f).height(20.dp))
                    com.gaatho.rent.core.ui.components.AppShimmerBox(modifier = Modifier.fillMaxWidth(0.4f).height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        com.gaatho.rent.core.ui.components.AppShimmerBox(modifier = Modifier.width(60.dp).height(24.dp), shape = CircleShape)
                        com.gaatho.rent.core.ui.components.AppShimmerBox(modifier = Modifier.width(80.dp).height(24.dp), shape = CircleShape)
                    }
                }
            }
        }
    }
}
