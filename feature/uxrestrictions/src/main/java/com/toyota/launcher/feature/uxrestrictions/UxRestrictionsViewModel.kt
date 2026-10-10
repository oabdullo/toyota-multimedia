package com.toyota.launcher.feature.uxrestrictions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class UxRestrictionsViewModel @Inject constructor(
    repo: UxRestrictionsRepository,
) : ViewModel() {
    val state: StateFlow<UxRestrictionsSnapshot> = repo.observe()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            UxRestrictionsSnapshot.Unrestricted,
        )
}
