/*
 * SPDX-FileCopyrightText: 2023 IacobIacob01
 * SPDX-License-Identifier: Apache-2.0
 */

package com.dot.gallery.feature_node.presentation.mediaview.components.media

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.dot.gallery.core.Constants.DEFAULT_TOP_BAR_ANIMATION_DURATION
import com.dot.gallery.core.Settings
import com.dot.gallery.core.decoder.EncryptedRegionDecoder
import com.dot.gallery.core.presentation.components.util.LocalBatteryStatus
import com.dot.gallery.core.presentation.components.util.ProvideBatteryStatus
import com.dot.gallery.core.presentation.components.util.swipe
import com.dot.gallery.feature_node.data.data_source.KeychainHolder
import com.dot.gallery.feature_node.domain.model.Media
import com.dot.gallery.feature_node.domain.util.asSubsamplingImage
import com.dot.gallery.feature_node.domain.util.getUri
import com.dot.gallery.feature_node.domain.util.isEncrypted
import com.dot.gallery.feature_node.presentation.util.GlideInvalidation
import com.dot.gallery.feature_node.presentation.util.rememberFeedbackManager
import com.github.panpf.sketch.rememberAsyncImagePainter
import com.github.panpf.sketch.request.ComposableImageRequest
import com.github.panpf.zoomimage.GlideZoomAsyncImage
import com.github.panpf.zoomimage.ZoomImage
import com.github.panpf.zoomimage.compose.glide.ExperimentalGlideComposeApi
import com.github.panpf.zoomimage.rememberGlideZoomState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.target.Target
import android.graphics.drawable.Drawable
@OptIn(ExperimentalGlideComposeApi::class,
    com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi::class
)
@Stable
@Composable
fun <T: Media> BoxScope.ZoomablePagerImage(
    modifier: Modifier = Modifier,
    media: T,
    uiEnabled: Boolean,
    rotationDisabled: Boolean,
    onImageRotated: (newRotation: Int) -> Unit,
    onItemClick: () -> Unit,
    onSwipeDown: () -> Unit
) {
    val context = LocalContext.current
    val feedbackManager = rememberFeedbackManager()
    var isRotating by rememberSaveable(media) { mutableStateOf(false) }
    var currentRotation by rememberSaveable(media) { mutableIntStateOf(0) }
    val rotationAnimation by animateFloatAsState(
        targetValue = if (isRotating) 90f else 0f,
        label = "rotationAnimation"
    )

    val isServerFile = media.path.startsWith("http")
    val mainModel = if (isServerFile) media.path else media.getUri()
    val thumbnailModel = media.getUri()

    // --- БЛЮР ФОН ---
    // Це залишаємо як є, воно приховує чорний фон поки вантажиться основне фото
    ProvideBatteryStatus {
        val allowBlur by Settings.Misc.rememberAllowBlur()
        val isPowerSavingMode = LocalBatteryStatus.current.isPowerSavingMode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && allowBlur && !isPowerSavingMode) {
            val blurAlpha by animateFloatAsState(
                animationSpec = tween(DEFAULT_TOP_BAR_ANIMATION_DURATION),
                targetValue = if (uiEnabled) 0.7f else 0f,
                label = "blurAlpha"
            )
            GlideImage(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(blurAlpha)
                    .blur(100.dp),
                model = thumbnailModel,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                requestBuilderTransform = {
                    it.override(200)
                        .signature(GlideInvalidation.signature(media))
                }
            )
        }
    }

    val zoomState = rememberGlideZoomState()
    val scope = rememberCoroutineScope()

    if (media.isEncrypted && !isServerFile) {
        // ... (Код для зашифрованих файлів залишаємо без змін)
        val painter = rememberAsyncImagePainter(
            request = ComposableImageRequest(media.getUri().toString()) {
                crossfade(durationMillis = 200)
                setExtra(
                    key = "mediaKeyPreviewEnc",
                    value = media.idLessKey,
                )
                setExtra("realMimeType", media.mimeType)
            },
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.None,
        )
        val keychainHolder = remember { KeychainHolder(context) }
        LaunchedEffect(zoomState.subsampling) {
            zoomState.subsampling.setRegionDecoders(
                listOf(
                    EncryptedRegionDecoder.Factory(
                        keychainHolder
                    )
                )
            )
            zoomState.setSubsamplingImage(media.asSubsamplingImage(context))
        }
        ZoomImage(
            zoomState = zoomState,
            painter = painter,
            modifier = Modifier
                .fillMaxSize()
                .swipe(onSwipeDown = onSwipeDown)
                .graphicsLayer { rotationZ = if (isRotating) rotationAnimation else 0f }
                .then(modifier),
            onTap = { onItemClick() },
            onLongPress = {
                if (!rotationDisabled) {
                    scope.launch {
                        isRotating = true
                        feedbackManager.vibrate()
                        currentRotation += 90
                        onImageRotated(currentRotation)
                        delay(350)
                        zoomState.zoomable.rotate(currentRotation)
                        isRotating = false
                    }
                }
            },
            alignment = Alignment.Center,
            contentDescription = media.label,
            scrollBar = null
        )
    } else {
        // --- ЛОГІКА ДЛЯ ЗВИЧАЙНИХ І СЕРВЕРНИХ ФОТО (Фінал) ---

        // 1. Стан для відстеження, чи завантажився оригінал
        var isFullImageLoaded by remember { mutableStateOf(false) }

        // 2. Анімація прозорості (alpha) для оригіналу
        val fullImageAlpha by animateFloatAsState(
            targetValue = if (isFullImageLoaded) 1f else 0f,
            animationSpec = tween(durationMillis = 300), // Плавність 300мс
            label = "imageFade"
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // ШАР 1: Мініатюра (Завжди видима знизу)
            GlideImage(
                model = thumbnailModel,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = if (isRotating) rotationAnimation else 0f }
                    .then(modifier),
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center,
                requestBuilderTransform = {
                    it.diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .dontTransform() // Важливо для позиціонування
                }
            )

            // ШАР 2: Оригінал (Накладається зверху з анімацією)
            GlideZoomAsyncImage(
                zoomState = zoomState,
                model = mainModel,
                modifier = Modifier
                    .fillMaxSize()
                    // 🔥 КЛЮЧОВИЙ МОМЕНТ: Керуємо видимістю через Compose
                    .alpha(fullImageAlpha)
                    .swipe(onSwipeDown = onSwipeDown)
                    .graphicsLayer { rotationZ = if (isRotating) rotationAnimation else 0f }
                    .then(modifier),
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center,
                onTap = { onItemClick() },
                onLongPress = {
                    if (!rotationDisabled) {
                        scope.launch {
                            isRotating = true
                            feedbackManager.vibrate()
                            currentRotation += 90
                            onImageRotated(currentRotation)
                            delay(350)
                            zoomState.zoomable.rotate(currentRotation)
                            isRotating = false
                        }
                    }
                },
                contentDescription = media.label,
                requestBuilderTransform = { requestBuilder ->
                    var builder = requestBuilder
                        .signature(GlideInvalidation.signature(media))
                        .priority(Priority.HIGH)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)

                    builder = builder.listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            isFullImageLoaded = true
                            return false
                        }
                    })

                    if (media.label.contains(".gif", ignoreCase = true)) {
                        builder = builder.decode(GifDrawable::class.java)
                    }
                    builder
                },
                scrollBar = null
            )
        }
    }
}