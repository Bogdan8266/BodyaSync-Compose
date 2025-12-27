package com.dot.gallery.feature_node.presentation.mediaview.components.video

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.dot.gallery.feature_node.data.data_source.KeychainHolder
import com.dot.gallery.feature_node.domain.model.Media
import com.dot.gallery.feature_node.domain.util.getUri
import com.dot.gallery.feature_node.domain.util.isEncrypted
import com.dot.gallery.feature_node.presentation.util.printDebug
import com.dot.gallery.feature_node.presentation.util.printWarning
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.time.Duration.Companion.seconds

@HiltViewModel(assistedFactory = VideoPlayerViewModel.Factory::class)
class VideoPlayerViewModel @AssistedInject constructor(
    @ApplicationContext private val appContext: Context,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
    @Assisted("media") private val media: Media,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("media") media: Media): VideoPlayerViewModel
    }

    data class PlaybackState(
        val isDecrypting: Boolean = false,
        val decryptFailed: Boolean = false,
        val ready: Boolean = false,
        val durationMs: Long = 0L,
        val positionMs: Long = 0L,
        val bufferedPercent: Int = 0,
        val frameRate: Float = 60f,
        val isPlaying: Boolean = false
    )

    private val keychainHolder = KeychainHolder(appContext)
    private var decryptedFile: File? = null
    private var initialSeekApplied = false
    private val _state = MutableStateFlow(PlaybackState(isDecrypting = media.isEncrypted))
    val state: StateFlow<PlaybackState> = _state
    var player: ExoPlayer = createExoPlayer()

    init {
        restoreFromSavedState()
        prepareMedia()
        startProgressLoop()
    }

    private fun createExoPlayer(): ExoPlayer {
        return ExoPlayer.Builder(appContext).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) markReady()
                    updateDuration(duration)
                }
                override fun onEvents(player: Player, events: Player.Events) {
                    updateDuration(player.duration)
                }
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _state.update { it.copy(isPlaying = isPlaying) }
                }
                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    if (!playWhenReady) _state.update { it.copy(isPlaying = false) }
                }
            })
        }
    }

    private fun restoreFromSavedState() {
        val positionRestore = savedStateHandle.get<Long>(KEY_POSITION) ?: 0L
        val wasPlayingRestore = savedStateHandle.get<Boolean>(KEY_PLAYING) ?: false
        if (positionRestore > 0) {
            _state.update { it.copy(positionMs = positionRestore, isPlaying = wasPlayingRestore) }
        }
    }

    private fun prepareMedia() {
        // ФІКС: Якщо посилання на сервер (http), то це не шифрований файл, ігноруємо isEncrypted
        val isServerFile = media.path.startsWith("http")

        if (media.isEncrypted && !isServerFile) {
            decryptAndPrepare()
        } else {
            // ФІКС: Беремо media.path (оригінал), якщо це сервер, інакше uri
            val uriToPlay = if (isServerFile) Uri.parse(media.path) else media.getUri()
            setAndPrepare(uriToPlay, media.mimeType)
            retrieveFrameRate(encrypted = false)
        }
    }

    private fun decryptAndPrepare() {
        viewModelScope.launch {
            _state.update { it.copy(isDecrypting = true, decryptFailed = false) }
            decryptedFile = withContext(Dispatchers.IO) {
                try { createDecryptedVideoFile(keychainHolder, media) }
                catch (t: Throwable) {
                    printWarning("Decrypt failed: ${t.message}")
                    null
                }
            }
            if (decryptedFile == null) {
                _state.update { it.copy(isDecrypting = false, decryptFailed = true) }
                return@launch
            }
            _state.update { it.copy(isDecrypting = false, decryptFailed = false) }
            setAndPrepare(Uri.fromFile(decryptedFile!!), media.mimeType)
            retrieveFrameRate(encrypted = true)
        }
    }

    private fun setAndPrepare(uri: Uri, mime: String?) {
        val existingUri = player.currentMediaItem?.localConfiguration?.uri
        if (existingUri == uri) return
        initialSeekApplied = false
        val item = MediaItem.Builder().setUri(uri).setMimeType(mime).build()
        player.setMediaItem(item)
        player.prepare()
    }

    private fun markReady() {
        if (!_state.value.ready) {
            _state.update { it.copy(ready = true) }
            if (!initialSeekApplied && _state.value.positionMs > 0) {
                player.seekTo(_state.value.positionMs)
                if (_state.value.isPlaying) player.play()
                initialSeekApplied = true
            }
        }
    }

    private fun updateDuration(duration: Long) {
        if (duration > 0 && duration != _state.value.durationMs) {
            _state.update { it.copy(durationMs = duration) }
        }
    }

    private fun startProgressLoop() {
        viewModelScope.launch {
            while (isActive) {
                _state.update { it.copy(positionMs = player.currentPosition, bufferedPercent = player.bufferedPercentage) }
                delay(1.seconds / 30)
            }
        }
    }

    private fun retrieveFrameRate(encrypted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val fps = try {
                MediaMetadataRetriever().use { r ->
                    // ФІКС: Підтримка HTTP URL
                    val isServerFile = media.path.startsWith("http")
                    if (encrypted) {
                        decryptedFile?.inputStream()?.use { r.setDataSource(it.fd) }
                    } else if (isServerFile) {
                        r.setDataSource(media.path, HashMap<String, String>())
                    } else {
                        r.setDataSource(appContext, media.getUri())
                    }
                    r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toFloat() ?: 60f
                }
            } catch (_: Exception) { 60f }
            _state.update { it.copy(frameRate = fps) }
        }
    }

    fun togglePlay() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _state.update { it.copy(positionMs = positionMs) }
    }

    fun setUserPlayWhenReady(play: Boolean, canAutoPlay: Boolean) {
        // Apply user intended state respecting autoplay rules externally
        val targetPlay = if (!play && canAutoPlay) false else play
        player.playWhenReady = targetPlay
        if (targetPlay) player.play() else player.pause()
        _state.update { it.copy(isPlaying = targetPlay) }
    }

    fun applyAudioFocusPreference(wantsFocus: Boolean) {
        val usage = if (wantsFocus) C.USAGE_MEDIA else C.USAGE_NOTIFICATION
        val contentType =
            if (wantsFocus) C.AUDIO_CONTENT_TYPE_MOVIE else C.AUDIO_CONTENT_TYPE_SONIFICATION
        val attrs = AudioAttributes.Builder()
            .setUsage(usage)
            .setContentType(contentType)
            .build()
        player.setAudioAttributes(attrs, /* handleAudioFocus = */ wantsFocus)
    }

    fun retryDecryption() {
        if (!_state.value.decryptFailed) return
        decryptAndPrepare()
    }

    @OptIn(UnstableApi::class)
    fun reattachFromComposition() {
        if (player.isReleased) {
            printDebug("Reattached to composition ${media.id}'s video")
            runCatching {
                player = createExoPlayer()
                restoreFromSavedState()
                prepareMedia()
                startProgressLoop()
                val isPlaying = savedStateHandle.get<Boolean>(KEY_PLAYING)
                player.playWhenReady = isPlaying ?: _state.value.isPlaying

                printDebug("Video after reattach: playWhenReady: ${player.playWhenReady}")
            }
        } else {
            printDebug("Skipped re-attaching to composition ${media.id}'s video. We are already there")
        }
    }

    @OptIn(UnstableApi::class)
    fun detachFromComposition() {
        printDebug("Cleared from composition ${media.id}'s video")
        runCatching {
            if (!player.isReleased) player.release()
        }
    }

    override fun onCleared() {
        // Persist position & play state
        savedStateHandle[KEY_POSITION] = player.currentPosition
        savedStateHandle[KEY_PLAYING] = player.isPlaying
        try {
            player.release()
        } catch (_: Throwable) {
        }
        decryptedFile?.delete()
        decryptedFile = null
        super.onCleared()
    }

    companion object {
        private const val KEY_POSITION = "positionMs"
        private const val KEY_PLAYING = "wasPlaying"
    }
}