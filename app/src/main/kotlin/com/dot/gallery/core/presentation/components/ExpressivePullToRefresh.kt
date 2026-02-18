package com.dot.gallery.core.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressivePullToRefreshIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {

    var targetProgress by remember { mutableFloatStateOf(0f) }


    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            while (true) {
                targetProgress = 1f
                delay(1000)
                targetProgress = 0f
                delay(1000)
            }
        } else {
            targetProgress = 0f
        }
    }

    val animatedRefreshingProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessVeryLow,
        ),
        label = "morphing_animation"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        // Логіка позиції і масштабу
        val pullFraction = state.distanceFraction.coerceIn(0f, 1.5f)
        val scaleFraction = if (isRefreshing) 1f else state.distanceFraction.coerceIn(0f, 1f)
        val alphaFraction = if (isRefreshing) 1f else (state.distanceFraction * 1.5f).coerceIn(0f, 1f)
        val yOffset = (state.distanceFraction * 120).coerceAtMost(150f)

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = if (isRefreshing || pullFraction > 0.1f) 4.dp else 0.dp,
            modifier = Modifier
                .padding(top = 16.dp) // Відступ зверху
                .graphicsLayer {
                    translationY = yOffset
                    scaleX = scaleFraction
                    scaleY = scaleFraction
                    alpha = alphaFraction
                }
                .size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                LoadingIndicator(

                    progress = {
                        if (isRefreshing) animatedRefreshingProgress
                        else (pullFraction * 0.5f).coerceIn(0f, 1f)
                    },
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}