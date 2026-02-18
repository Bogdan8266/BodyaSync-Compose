/*
 * SPDX-FileCopyrightText: 2023 IacobIacob01
 * SPDX-License-Identifier: Apache-2.0
 */

package com.dot.gallery.feature_node.presentation.timeline

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dokar.pinchzoomgrid.PinchZoomGridLayout
import com.dokar.pinchzoomgrid.rememberPinchZoomGridState
import com.dot.gallery.core.Constants.cellsList
import com.dot.gallery.core.LocalEventHandler
import com.dot.gallery.core.LocalMediaSelector
import com.dot.gallery.core.Settings.Misc.rememberGridSize
import com.dot.gallery.core.navigate
import com.dot.gallery.core.presentation.components.EmptyMedia
import com.dot.gallery.core.presentation.components.ExpressivePullToRefreshIndicator
import com.dot.gallery.core.presentation.components.ExpressiveScreenLoader
import com.dot.gallery.core.presentation.components.SelectionSheet
import com.dot.gallery.core.toggleNavigationBar
import com.dot.gallery.feature_node.domain.model.Media
import com.dot.gallery.feature_node.domain.model.MediaMetadataState
import com.dot.gallery.feature_node.domain.model.MediaState
import com.dot.gallery.feature_node.presentation.common.components.MediaGridView
import com.dot.gallery.feature_node.presentation.search.MainSearchBar
import com.dot.gallery.feature_node.presentation.timeline.components.TimelineNavActions
import com.dot.gallery.feature_node.presentation.util.LocalHazeState
import com.dot.gallery.feature_node.presentation.util.Screen
import com.dot.gallery.feature_node.presentation.util.selectedMedia
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun TimelineScreen(
    paddingValues: PaddingValues,
    isScrolling: MutableState<Boolean>,
    mediaState: State<MediaState<Media.UriMedia>>,
    metadataState: State<MediaMetadataState>,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    var canScroll by rememberSaveable { mutableStateOf(true) }
    var lastCellIndex by rememberGridSize()
    val eventHandler = LocalEventHandler.current
    val selector = LocalMediaSelector.current
    val selectionState = selector.isSelectionActive.collectAsStateWithLifecycle()
    // ФІКС: Збираємо selectedMedia як State, бо функція selectedMedia очікує State
    val selectedMediaState = selector.selectedMedia.collectAsStateWithLifecycle()

    val dpCacheWindow = LazyLayoutCacheWindow(aheadFraction = 2f, behindFraction = 2f)
    val pinchState = rememberPinchZoomGridState(
        cellsList = cellsList,
        initialCellsIndex = lastCellIndex,
        gridState = rememberLazyGridState(
            cacheWindow = dpCacheWindow
        )
    )
    // Pull to Refresh State
    val pullState = rememberPullToRefreshState()

    LaunchedEffect(pinchState.isZooming) {
        withContext(Dispatchers.IO) {
            canScroll = !pinchState.isZooming
            lastCellIndex = cellsList.indexOf(pinchState.currentCells)
        }
    }

    LaunchedEffect(selectionState.value) {
        eventHandler.toggleNavigationBar(!selectionState.value)
    }

    Box(
        modifier = Modifier.padding(
            start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
            end = paddingValues.calculateEndPadding(LocalLayoutDirection.current)
        )
    ) {
        Scaffold(
            topBar = {
                MainSearchBar(
                    isScrolling = isScrolling,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                    menuItems = { TimelineNavActions() },
                )
            }
        ) { it ->
            if (mediaState.value.isLoading && mediaState.value.media.isEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(top = it.calculateTopPadding())
                        .fillMaxSize()
                ) {
                    ExpressiveScreenLoader()
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refreshMedia() },
                    state = pullState,
                    modifier = Modifier.fillMaxSize(),
                    indicator = {
                        ExpressivePullToRefreshIndicator(
                            state = pullState,
                            isRefreshing = isRefreshing,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = it.calculateTopPadding())
                        )
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationY = pullState.distanceFraction * 150f
                            }
                    ) {
                        PinchZoomGridLayout(
                            state = pinchState,
                            modifier = Modifier.hazeSource(LocalHazeState.current)
                        ) {
                            MediaGridView(
                                mediaState = mediaState,
                                metadataState = metadataState,
                                paddingValues = remember(paddingValues, it) {
                                    PaddingValues(
                                        top = it.calculateTopPadding(),
                                        bottom = paddingValues.calculateBottomPadding() + 128.dp
                                    )
                                },
                                searchBarPaddingTop = remember(paddingValues) {
                                    paddingValues.calculateTopPadding()
                                },
                                showSearchBar = true,
                                allowSelection = true,
                                canScroll = canScroll,
                                enableStickyHeaders = true,
                                showMonthlyHeader = true,
                                isScrolling = isScrolling,
                                emptyContent = { EmptyMedia() },
                                sharedTransitionScope = sharedTransitionScope,
                                animatedContentScope = animatedContentScope,
                                onMediaClick = { media ->
                                    eventHandler.navigate(Screen.MediaViewScreen.idAndAlbum(media.id, -1L))
                                },
                            )
                        }
                    }
                }
            }
        }

        // ФІКС: Передаємо правильні параметри в selectedMedia
        val selectedMediaList by selectedMedia(
            media = mediaState.value.media,
            selectedSet = selectedMediaState
        )

        SelectionSheet(
            modifier = Modifier.align(Alignment.BottomEnd),
            // ФІКС: Перетворюємо список на MediaState, бо SelectionSheet цього очікує
            allMedia = MediaState(media = mediaState.value.media),
            selectedMedia = selectedMediaList
        )
    }
}