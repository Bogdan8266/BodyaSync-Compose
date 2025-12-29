package com.dot.gallery.feature_node.presentation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun ExpressiveScreenLoader() {
    // Стан для анімації прогресу
    var targetProgress by remember { mutableFloatStateOf(0.1f) }

    // Запускаємо цикл: заповнити -> почекати -> очистити
    LaunchedEffect(Unit) {
        while (true) {
            targetProgress = 1f
            delay(1500) // Час, поки воно повне
            targetProgress = 0.05f
            delay(1500) // Час, поки воно пусте
        }
    }

    // Твоя анімація з фізикою пружини
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessVeryLow,
            visibilityThreshold = 1 / 1000f,
        ),
        label = "expressive_loader"
    )

    // Центруємо на весь екран
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .width(80.dp)  // Ширина індикатора
                .height(6.dp), // Товщина лінії
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    }
}