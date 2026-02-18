
package com.dot.gallery.feature_node.presentation.mediaview.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dot.gallery.R
import com.dot.gallery.feature_node.domain.model.Media

@Composable
fun MediaViewAppBar(
    showUI: Boolean,
    showInfo: Boolean,
    showDate: Boolean,
    currentDate: AnnotatedString,
    paddingValues: PaddingValues,
    onGoBack: () -> Unit,
    onShowInfo: () -> Unit,
    currentMedia: Media?,
    isLocked: Boolean = false,
    onLock: () -> Unit = {},
    showRotationHelper: Boolean = false,
    rotateImage: () -> Unit = {},
) {
    val secondaryIconColor = Color.White.copy(alpha = 0.7f)

    AnimatedVisibility(
        visible = showUI,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // ПОСИЛЕНИЙ ГРАДІЄНТ ЗВЕРХУ
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.9f), // Майже чорний зверху
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- BACK BUTTON ---
                IconButton(
                    onClick = onGoBack,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = secondaryIconColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back_cd)
                    )
                }

                // --- DATE ---
                if (showDate) {
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // --- INFO BUTTON (Замок видалено) ---
                if (showInfo) {
                    IconButton(
                        onClick = onShowInfo,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = secondaryIconColor
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = stringResource(id = R.string.media_info)
                        )
                    }
                } else {
                    // Балансувальний Spacer, якщо кнопки інфо немає, щоб дата була по центру
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }
    }
}