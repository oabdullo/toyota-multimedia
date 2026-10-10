package com.toyota.launcher.feature.media

import android.media.session.MediaController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val repo: MediaSessionRepository,
) : ViewModel() {

    private val controllerFlow = MutableStateFlow<MediaController?>(null)

    init {
        viewModelScope.launch {
            repo.observeTopSession().collect { controllerFlow.value = it }
        }
    }

    /**
     * We re-snapshot whenever the controller changes OR a tick (every metadata
     * delta) - in a production app we'd register MediaController.Callback
     * for surgical updates. For a launcher card, controller-level resnap is fine.
     */
    val state: StateFlow<MediaState> = combine(
        controllerFlow, controllerFlow,
    ) { c, _ -> repo.snapshot(c) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MediaState())

    fun togglePlay() {
        val c = controllerFlow.value ?: return
        val isPlaying = state.value.isPlaying
        if (isPlaying) c.transportControls.pause() else c.transportControls.play()
    }

    fun skipNext() = controllerFlow.value?.transportControls?.skipToNext()
    fun skipPrev() = controllerFlow.value?.transportControls?.skipToPrevious()
}
