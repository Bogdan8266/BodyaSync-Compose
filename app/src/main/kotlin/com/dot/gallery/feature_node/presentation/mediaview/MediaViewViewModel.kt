package com.dot.gallery.feature_node.presentation.mediaview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.dot.gallery.core.workers.rotateImage
import com.dot.gallery.feature_node.domain.model.Media
import com.dot.gallery.feature_node.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MediaViewViewModel @Inject constructor(
    private val workManager: WorkManager,
    private val repository: MediaRepository
) : ViewModel() {

    private val _uiEvents = MutableSharedFlow<MediaViewEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<MediaViewEvent> = _uiEvents

    private var rotateWorkId: UUID? = null

    fun rotateImage(media: Media, degrees: Int) {
        val id = workManager.rotateImage(media, degrees)
        rotateWorkId = id
        observeRotateWork(id)
    }

    private fun observeRotateWork(id: UUID) {
        viewModelScope.launch {
            workManager.getWorkInfoByIdFlow(id).filterNotNull().collect { info ->
                if (info.state.isFinished) {
                    if (info.state == WorkInfo.State.SUCCEEDED) {
                        delay(300) // wait for media store to be updated
                        _uiEvents.emit(MediaViewEvent.ScrollToFirstPage)
                    }
                    rotateWorkId = null
                }
            }
        }
    }


    // У класі ViewModel
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun refreshMedia() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // Тут викликаємо твій репозиторій
            // Якщо у repository є метод sync() або forceUpdate(), викликай його.
            // Якщо немає, можна просто викликати updateInternalDatabase()
            repository.updateInternalDatabase()

            // Імітація затримки, щоб анімація не зникала миттєво, якщо сервер відповів за 10мс
            delay(1500)
            _isRefreshing.value = false
        }
    }
    sealed interface MediaViewEvent {
        data object ScrollToFirstPage : MediaViewEvent
    }
}