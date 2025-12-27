package com.dot.gallery.feature_node.presentation.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import com.dot.gallery.core.Position
import com.dot.gallery.core.SettingsEntity

@Composable
fun rememberSeekPreference(
    title: String,
    summary: String? = null,
    icon: ImageVector? = null,
    currentValue: Float,
    minValue: Float,
    maxValue: Float,
    step: Int = 1,
    seekSuffix: String? = null,
    onSeek: (Float) -> Unit,
    screenPosition: Position = Position.Middle
): SettingsEntity.SeekPreference {
    return remember(title, currentValue) {
        SettingsEntity.SeekPreference(
            title = title,
            summary = summary,
            icon = icon,
            currentValue = currentValue,
            minValue = minValue,
            maxValue = maxValue,
            step = step,
            seekSuffix = seekSuffix,
            onSeek = onSeek,
            screenPosition = screenPosition
        )
    }
}