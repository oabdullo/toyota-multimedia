package com.toyota.launcher.feature.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class WeatherViewModel @Inject constructor(
    repo: WeatherRepository,
) : ViewModel() {
    val state: StateFlow<WeatherState> = repo.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeatherState())
}
