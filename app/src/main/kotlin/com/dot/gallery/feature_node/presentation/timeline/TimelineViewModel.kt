package com.dot.gallery.feature_node.presentation.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dot.gallery.core.Resource
import com.dot.gallery.feature_node.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val repository: MediaRepository
) : ViewModel() {


    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun refreshMedia() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.updateInternalDatabase()
            delay(1500)
            _isRefreshing.value = false
        }
    }
}