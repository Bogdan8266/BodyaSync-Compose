/*
 * SPDX-FileCopyrightText: 2023 IacobIacob01
 * SPDX-License-Identifier: Apache-2.0
 */


package com.dot.gallery.feature_node.presentation.mediaview.components.video

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeMute
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.media3.exoplayer.ExoPlayer
import com.dot.gallery.R
import com.dot.gallery.feature_node.domain.model.PlaybackSpeed
import com.dot.gallery.feature_node.presentation.util.formatMinSec
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerController(
    paddingValues: PaddingValues,
    player: ExoPlayer,
    isPlaying: MutableState<Boolean>,
    currentTime: MutableLongState,
    totalTime: Long,
    buffer: Int,
    toggleRotate: () -> Unit,
    frameRate: Float
) {
    val scope = rememberCoroutineScope()
    val secondaryIconColor = Color.White.copy(alpha = 0.7f)

    Box(
        modifier = Modifier
            .zIndex(10f)
            .fillMaxSize()
    ) {
        // --- НИЖНЯ ПАНЕЛЬ (ГРАДІЄНТ + СКРАББЕР) ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                // ПОСИЛЕНИЙ ГРАДІЄНТ ЗНИЗУ
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.95f) // Дуже чорний внизу
                        ),
                    )
                )
                .padding(horizontal = 16.dp)
                .padding(top = 64.dp) // Більше градієнта зверху
                // ВАЖЛИВИЙ ФІКС: Піднімаємо скраббер вгору, щоб він не накладався на кнопки Share/Edit
                // 100.dp - це приблизна висота нижнього бару з кнопками.
                // Можна підлаштувати це число (90.dp, 120.dp) під свій екран.
                .padding(bottom = paddingValues.calculateBottomPadding() + 90.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            // ... (Логіка звуку/швидкості така сама) ...
            var isMuted by rememberSaveable { mutableStateOf(player.volume == 0f) }
            var currentVolume by rememberSaveable { mutableFloatStateOf(player.volume) }

            LaunchedEffect(LocalConfiguration.current, player.currentMediaItem, isMuted) {
                player.volume = if (isMuted) 0f else currentVolume
            }

            var auto by rememberSaveable { mutableStateOf(false) }
            var showMenu by rememberSaveable { mutableStateOf(false) }
            var playbackSpeed by rememberSaveable { mutableFloatStateOf(1f) }
            val ctx = LocalResources.current
            val playbackSpeeds = remember(frameRate) {
                listOf(
                    PlaybackSpeed(1f / (frameRate / 30f), ctx.getString(R.string.auto), true),
                    PlaybackSpeed(0.125f, "0.125x"),
                    PlaybackSpeed(0.25f, "0.25x"),
                    PlaybackSpeed(0.5f, "0.5x"),
                    PlaybackSpeed(1f, "1x"),
                    PlaybackSpeed(2f, "2x")
                )
            }
            LaunchedEffect(playbackSpeed) {
                player.setPlaybackSpeed(playbackSpeed)
                showMenu = false
            }

            var isScrubbing by rememberSaveable { mutableStateOf(false) }
            var wasPlayingBeforeScrub by remember { mutableStateOf(false) }
            var sliderValue by rememberSaveable { mutableFloatStateOf(currentTime.longValue.toFloat()) }

            LaunchedEffect(currentTime.longValue, isScrubbing) {
                if (!isScrubbing) {
                    sliderValue = currentTime.longValue.toFloat()
                }
            }

            // Кнопки (Швидкість, Звук, Поворот)
            Box(contentAlignment = Alignment.TopEnd) {
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    playbackSpeeds.forEach { speed ->
                        DropdownMenuItem(
                            modifier = Modifier.padding(end = 16.dp),
                            onClick = {
                                playbackSpeed = speed.speed
                                auto = speed.isAuto
                            },
                            leadingIcon = {
                                RadioButton(
                                    selected = (playbackSpeed == speed.speed && !speed.isAuto) || (speed.isAuto && auto),
                                    onClick = {
                                        playbackSpeed = speed.speed
                                        auto = speed.isAuto
                                    }
                                )
                            },
                            text = { Text(text = speed.label) }
                        )
                    }
                }
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(
                        imageVector = Icons.Outlined.Speed,
                        tint = secondaryIconColor,
                        contentDescription = stringResource(R.string.change_playback_speed_cd)
                    )
                }
            }

            IconButton(
                onClick = {
                    if (isMuted) {
                        player.volume = currentVolume
                        isMuted = false
                    } else {
                        currentVolume = player.volume
                        player.volume = 0f
                        isMuted = true
                    }
                }
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.AutoMirrored.Outlined.VolumeMute else Icons.AutoMirrored.Outlined.VolumeUp,
                    tint = secondaryIconColor,
                    contentDescription = stringResource(R.string.toggle_audio_cd)
                )
            }

            IconButton(onClick = { toggleRotate() }) {
                Icon(
                    imageVector = Icons.Outlined.ScreenRotation,
                    tint = secondaryIconColor,
                    contentDescription = stringResource(R.string.rotate_screen_cd)
                )
            }

            // Timeline Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    modifier = Modifier.width(52.dp),
                    text = sliderValue.toLong().formatMinSec(),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Box(Modifier.weight(1f)) {
                    WavyVideoScrubber(
                        value = sliderValue,
                        total = totalTime.toFloat(),
                        buffer = buffer.toFloat(),
                        onScrub = { newVal ->
                            if (!isScrubbing) {
                                isScrubbing = true
                                wasPlayingBeforeScrub = isPlaying.value
                                if (player.isPlaying) player.pause()
                            }
                            sliderValue = newVal
                        },
                        onScrubFinished = {
                            scope.launch {
                                val target = sliderValue.toLong().coerceIn(0L, totalTime)
                                if (player.currentPosition != target) {
                                    player.seekTo(target)
                                }
                                currentTime.longValue = target
                                isScrubbing = false
                                if (wasPlayingBeforeScrub) {
                                    player.playWhenReady = true
                                    player.play()
                                    isPlaying.value = true
                                } else {
                                    player.playWhenReady = false
                                    isPlaying.value = false
                                }
                            }
                        }
                    )
                }

                Text(
                    modifier = Modifier.width(52.dp),
                    text = totalTime.formatMinSec(),
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryIconColor,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Center Play Button
        Box(modifier = Modifier.align(Alignment.Center)) {
            ExpressivePlayButton(
                isPlaying = isPlaying.value && player.isPlaying,
                onClick = {
                    val newState = !isPlaying.value
                    isPlaying.value = newState
                    if (newState) {
                        player.playWhenReady = true
                        player.play()
                    } else {
                        player.pause()
                    }
                }
            )
        }
    }
}
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WavyVideoScrubber(
    value: Float,
    total: Float,
    buffer: Float,
    onScrub: (Float) -> Unit,
    onScrubFinished: () -> Unit
) {
    val progress = if (total > 0) value / total else 0f

    // Плавна анімація прогресу
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "wavyProgress"
    )

    // Стиль лінії (товста і заокруглена)
    val thickStrokeWidth = with(LocalDensity.current) { 8.dp.toPx() }
    val thickStroke = remember(thickStrokeWidth) {
        Stroke(width = thickStrokeWidth, cap = StrokeCap.Round)
    }

    // Змінна для ширини компонента
    var width by remember { mutableFloatStateOf(1f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp) // Збільшена висота для зручності дотику
            .onSizeChanged { width = it.width.toFloat() }
            .pointerInput(Unit) {
                // Обробка натискання (Tap)
                detectTapGestures(
                    onPress = { offset ->
                        val newProgress = (offset.x / width).coerceIn(0f, 1f)
                        onScrub(newProgress * total)
                        tryAwaitRelease() // Чекаємо відпускання пальця
                        onScrubFinished()
                    }
                )
            }
            .pointerInput(Unit) {
                // Обробка перетягування (Drag)
                detectHorizontalDragGestures(
                    onDragEnd = { onScrubFinished() },
                    onDragCancel = { onScrubFinished() },
                    onDragStart = { offset ->
                        val newProgress = (offset.x / width).coerceIn(0f, 1f)
                        onScrub(newProgress * total)
                    }
                ) { change, _ ->
                    val newProgress = (change.position.x / width).coerceIn(0f, 1f)
                    onScrub(newProgress * total)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Сам індикатор (тільки для відображення)
        LinearWavyProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp), // Висота самої хвилі
            stroke = thickStroke,
            trackStroke = thickStroke,
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color.White.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun ExpressivePlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope() // Для таймера анімації

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Додатковий стан для "удару" при швидкому кліку
    var isAnimatingClick by remember { mutableStateOf(false) }

    // Анімація ширини
    // Логіка: Якщо тиснеш АБО тільки що клікнув -> розширюємось
    val width by animateDpAsState(
        targetValue = if (isPressed || isAnimatingClick) 115.dp else 90.dp,
        animationSpec = spring(
            dampingRatio = 0.4f, // Пружний відскок
            stiffness = Spring.StiffnessMedium
        ),
        label = "buttonWidth"
    )

    // Анімація форми (без змін)
    val cornerPercent by animateIntAsState(
        targetValue = if (isPlaying) 50 else 30,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonShape"
    )

    // Анімація кольору (без змін)
    val containerColor by animateColorAsState(
        targetValue = if (isPlaying)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
        else
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f),
        label = "buttonColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isPlaying)
            MaterialTheme.colorScheme.onPrimaryContainer
        else
            MaterialTheme.colorScheme.onTertiaryContainer,
        label = "iconColor"
    )

    Box(
        modifier = Modifier
            .size(width = width, height = 64.dp)
            .clip(RoundedCornerShape(cornerPercent))
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                // 1. Хаптик (CONFIRM, як ти хотів)
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)

                // 2. Виконуємо дію
                onClick()

                // 3. Запускаємо візуальний "поштовх"
                scope.launch {
                    isAnimatingClick = true
                    // Чекаємо 100мс, щоб пружина встигла візуально розширити кнопку
                    delay(100)
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    isAnimatingClick = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Filled.PauseCircleFilled else Icons.Filled.PlayCircleFilled,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(32.dp)
        )
    }
}
