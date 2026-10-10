package com.toyota.launcher.feature.topstatus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class TopStatusViewModel @Inject constructor(
    repo: TopStatusRepository,
) : ViewModel() {
    val state: StateFlow<TopStatusState> = repo.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TopStatusState())
}
