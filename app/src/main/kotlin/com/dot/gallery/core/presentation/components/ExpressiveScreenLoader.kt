package com.dot.gallery.core.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveScreenLoader(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    // Стан для анімації (0 -> 1 -> 0)
    var targetProgress by remember { mutableStateOf(0.1f) }

    // Запускаємо цикл анімації "дихання"
    LaunchedEffect(Unit) {
        while (true) {
            targetProgress = 1f // Заповнюємо
            delay(1500) // Чекаємо поки пружина відпрацює
            targetProgress = 0.05f // Спустошуємо (майже до нуля, щоб не зникав повністю)
            delay(1500)
        }
    }

    // Твоя улюблена фізика пружини
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy, // Плавність без відскоку
            stiffness = Spring.StiffnessVeryLow,        // Дуже м'яко і повільно
            visibilityThreshold = 1 / 1000f,
        ),
        label = "ExpressiveLoaderAnimation"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator(
            modifier = Modifier.size(48.dp),
        )
    }
}