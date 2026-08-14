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
import coil3.compose.AsyncImage
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.AppDimensions
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.designsystem.Spacing
import com.gaatho.rent.core.designsystem.Radius
import com.gaatho.rent.core.designsystem.ExtendedColorHex
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.features.property.domain.model.Property
// Removed AddPropertyBottomSheet imports
import com.gaatho.rent.core.utils.toImageBitmap
import com.gaatho.rent.core.ui.components.AppDialog
import com.gaatho.rent.core.ui.components.AppFilterChips
import com.gaatho.rent.core.ui.components.AppStatusBadge
import com.gaatho.rent.core.ui.components.AppSearchBar
import com.gaatho.rent.core.ui.components.AppTopBarCircleIconButton
import com.gaatho.rent.core.ui.components.AppCard
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
                actions = {
                    AppTopBarCircleIconButton(
                        icon = Icons.Default.Add,
                        onClick = { onAction(OnAddPropertyClicked) }
                    )
                },
                modifier = Modifier.statusBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surface // Solid white as per Figma
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = MaterialTheme.colorScheme.surface
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
                } else if (isEmpty && searchQuery.isEmpty() && state.selectedFilter == PropertyListFilters.All) {
                    EmptyPropertiesState(
                        onAddProperty = { onAction(PropertyListAction.OnAddPropertyClicked) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            bottom = 100.dp
                        )
                    ) {
                        item {
                            AppSearchBar(
                                query = searchQuery,
                                onQueryChange = onSearchQueryChanged,
                                placeholderText = stringResource(Res.string.search_properties_hint),
                                suggestions = emptyList(),
                                onSuggestionSelected = {},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                                    .height(44.dp) // Figma Height
                            )
                        }

                        item {
                            val filterOptions = listOf(
                                PropertyListFilters.All,
                                PropertyListFilters.Residential,
                                PropertyListFilters.Commercial
                            )
                            val displayLabels = listOf(
                                "${stringResource(Res.string.filter_all_locations)} (${pagedProperties.itemCount})",
                                stringResource(Res.string.filter_residential),
                                stringResource(Res.string.filter_commercial)
                            )
                            val selectedIndex = filterOptions.indexOf(state.selectedFilter).coerceAtLeast(0)

                            AppFilterChips(
                                options = displayLabels,
                                selectedIndex = selectedIndex,
                                onOptionSelected = { index ->
                                    onAction(OnFilterSelected(filterOptions[index]))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        items(count = pagedProperties.itemCount) { index ->
                            val property = pagedProperties[index]
                            if (property != null) {
                                PropertyRowItem(
                                    property = property,
                                    onClick = { onAction(PropertyListAction.OnPropertyClicked(property.id)) },
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                )
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
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = 0.dp // Set to 0 to avoid Material 3 surface tint; AppCard already applies figmaCardShadow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top // Figma: items-start
        ) {
            val imageUrl = property.imageUrl
            var isImageRendered = false

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer),
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
                        val bitmap = bytes?.toImageBitmap()
                        if (bitmap != null) {
                            isImageRendered = true
                            Image(
                                bitmap = bitmap,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else if (imageUrl.startsWith("http")) {
                        isImageRendered = true
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                if (!isImageRendered) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = property.name,
                    style = MaterialTheme.typography.titleLarge, // 13sp Bold — Figma "Baluwatar House"
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = property.address,
                    style = MaterialTheme.typography.bodySmall, // 10sp Regular — Figma "Baluwatar, Kathmandu"
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Units Badge
                    AppStatusBadge(
                        label = stringResource(Res.string.units_label, property.totalUnits),
                        containerColor = AppColors.EmeraldAccentLight,
                        contentColor = MaterialTheme.colorScheme.primary,
                        fontSize = 9.5.sp,
                        verticalPadding = 4.dp
                    )

                    // Status
                    val statusText = if (property.vacUnits > 0) 
                        stringResource(Res.string.vacant_label, property.vacUnits)
                    else 
                        stringResource(Res.string.occupied_label, property.occUnits)
                    
                    val statusColor = if (property.vacUnits > 0) AppColors.Warning else MaterialTheme.colorScheme.primary

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium, // 10sp Medium — Figma "4 Occupied"
                            color = statusColor
                        )
                    )
                }

                Text(
                    text = stringResource(Res.string.price_per_month, property.priceFormatted),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium, // 10sp Medium — Figma "NPR 1,25,000 / mo"
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(top = 2.dp)
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
            onNavigateToAddProperty = {},
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
