@file:OptIn(ExperimentalMaterial3Api::class)

package com.gaatho.rent.features.property.presentation.list

import com.gaatho.rent.core.ui.components.*

import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.*

// Removed AddPropertyBottomSheet imports

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.gaatho.rent.core.designsystem.AppColors
import com.gaatho.rent.core.designsystem.AppDimensions
import com.gaatho.rent.core.designsystem.RentManagerTheme
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.core.ui.components.AppBadge
import com.gaatho.rent.core.ui.components.AppBadgeType
import com.gaatho.rent.core.ui.components.AppFilterChips
import com.gaatho.rent.core.ui.components.AppListItemSurface
import com.gaatho.rent.core.ui.components.AppSearchBar
import com.gaatho.rent.core.utils.toImageBitmap
import com.gaatho.rent.features.property.presentation.list.PropertyListAction.OnAddPropertyClicked
import com.gaatho.rent.features.property.presentation.list.PropertyListAction.OnFilterSelected
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.filter_all_locations
import rentmanagerapp.shared.generated.resources.filter_commercial
import rentmanagerapp.shared.generated.resources.filter_residential
import rentmanagerapp.shared.generated.resources.occupied_label
import rentmanagerapp.shared.generated.resources.price_per_month
import rentmanagerapp.shared.generated.resources.properties_title
import rentmanagerapp.shared.generated.resources.retry
import rentmanagerapp.shared.generated.resources.search_properties_hint
import rentmanagerapp.shared.generated.resources.units_label
import rentmanagerapp.shared.generated.resources.vacant_label
import rentmanagerapp.shared.generated.resources.welcome_property_empty_desc
import rentmanagerapp.shared.generated.resources.welcome_property_empty_title

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
    val searchText by viewModel.searchText.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

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

    // Refresh pager each time this screen is composed (e.g. after navigating back)
    LaunchedEffect(Unit) {
        pagedProperties.refresh()
    }

    PropertyListContent(
        state = state,
        searchText = searchText,
        isSearching = isSearching,
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
    searchText: String = "",
    isSearching: Boolean = false,
    pagedProperties: LazyPagingItems<PropertyDisplayModel>? = null,
    onAction: (PropertyListAction) -> Unit,
    onSearchQueryChanged: (String) -> Unit = {},
    onNavigateToAddProperty: () -> Unit,
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
                title = stringResource(Res.string.properties_title),
                modifier = Modifier.statusBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surface
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isFabVisible && state.isOnline,
                enter = slideInVertically(initialOffsetY = { it * 2 }),
                exit = slideOutVertically(targetOffsetY = { it * 2 })
            ) {
                ExtendedFloatingActionButton(
                    onClick = { onAction(OnAddPropertyClicked) },
                    shape = RoundedCornerShape(50),
                    containerColor = AppColors.EmeraldAccent,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(Res.string.property_add_button)) },
                    expanded = true
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!state.isOnline && (pagedProperties?.itemCount ?: 0) == 0) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    com.gaatho.rent.core.ui.components.AppEmptyState(
                        icon = Icons.Outlined.WifiOff,
                        title = "No Internet Connection",
                        description = "Please check your network settings and try again."
                    )
                }
            } else {
            AppSearchBar(
                query = searchText,
                onQueryChange = onSearchQueryChanged,
                placeholderText = stringResource(Res.string.search_properties_hint),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .height(44.dp)
            )

            val filterOptions = listOf(
                PropertyListFilters.All,
                PropertyListFilters.Residential,
                PropertyListFilters.Commercial
            )
            val displayLabels = listOf(
                "${stringResource(Res.string.filter_all_locations)} (${pagedProperties?.itemCount ?: 0})",
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f) // Ensure this takes remaining space and constrains children
                    .widthIn(max = 800.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                if (pagedProperties == null) {
                    PropertySkeletonLoadingState()
                } else {
                    val refreshState = pagedProperties.loadState.refresh
                    val appendState = pagedProperties.loadState.append
                    val isEmpty = pagedProperties.itemCount == 0

                    if (isSearching || refreshState is LoadState.Loading) {
                        PropertySkeletonLoadingState()
                    } else if (refreshState is LoadState.Error) {
                        ErrorState(
                            message = ErrorMessageExtractor.extract(refreshState.error, "Failed to load properties"),
                            onRetry = { pagedProperties.retry() },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (isEmpty && searchText.isEmpty() && state.selectedFilter == PropertyListFilters.All) {
                        EmptyPropertiesState(
                            onAddProperty = { onAction(PropertyListAction.OnAddPropertyClicked) },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(
                                count = pagedProperties.itemCount,
                                key = pagedProperties.itemKey { it.id },
                                contentType = pagedProperties.itemContentType { "propertyRow" }
                            ) { index ->
                                val property = pagedProperties[index]
                                if (property != null) {
                                    PropertyRowItem(
                                        property = property,
                                        onAction = onAction,
                                        modifier = Modifier.animateItem()
                                    )
                                } else {
                                    // High-quality skeleton loader for paginated items (placeholders)
                                    PropertyRowPlaceholder()
                                }
                            }

                            if (appendState is LoadState.Loading) {
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
            } // end else for offline empty state
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
        onAction: (PropertyListAction) -> Unit,
        modifier: Modifier = Modifier
    ) {
        Column(modifier = modifier) {
        AppListItemSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            onClick = { onAction(PropertyListAction.OnPropertyClicked(property.id)) },
            shadowElevation = 0.dp // Set to 0 to avoid Material 3 surface tint; AppCard already applies figmaCardShadow
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
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
                            com.gaatho.rent.core.ui.components.AppAsyncImage(
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CardTitle(
                        text = property.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    BodySmallText(
                        text = property.address,
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
                        AppBadge(
                            text = stringResource(Res.string.units_label, property.totalUnits),
                            type = AppBadgeType.BRAND
                        )

                        // Status
                        val statusText = if (property.vacUnits > 0)
                            stringResource(Res.string.vacant_label, property.vacUnits)
                        else
                            stringResource(Res.string.occupied_label, property.occUnits)

                        AppBadge(
                            text = statusText,
                            type = if (property.vacUnits > 0) AppBadgeType.WARNING else AppBadgeType.SUCCESS
                        )
                    }

                    BodySmallText(
                        text = stringResource(Res.string.price_per_month, property.priceFormatted),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp).align(Alignment.CenterVertically)
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

/**
 * High-quality placeholder skeleton for a property list item.
 * Shown when Paging 3 placeholders are enabled.
 */
@Composable
fun PropertyRowPlaceholder(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            com.gaatho.rent.core.ui.components.AppShimmerBox(
                modifier = Modifier.size(90.dp),
                shape = RoundedCornerShape(10.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.gaatho.rent.core.ui.components.AppShimmerBox(
                    modifier = Modifier.fillMaxWidth(0.6f).height(20.dp)
                )
                com.gaatho.rent.core.ui.components.AppShimmerBox(
                    modifier = Modifier.fillMaxWidth(0.4f).height(14.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    com.gaatho.rent.core.ui.components.AppShimmerBox(
                        modifier = Modifier.width(60.dp).height(24.dp),
                        shape = CircleShape
                    )
                    com.gaatho.rent.core.ui.components.AppShimmerBox(
                        modifier = Modifier.width(80.dp).height(24.dp),
                        shape = CircleShape
                    )
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outlineVariant
        )
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
        com.gaatho.rent.core.ui.components.AppEmptyState(
            icon = null,
            title = stringResource(Res.string.welcome_property_empty_title),
            description = stringResource(Res.string.welcome_property_empty_desc)
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
        BodyText(
            text = ErrorMessageExtractor.extractFromString(message),
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
