package com.toyota.launcher.feature.hvac

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HvacViewModel @Inject constructor(
    private val repo: HvacRepository,
) : ViewModel() {

    val state: StateFlow<HvacState> = repo.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HvacState())

    fun driverTempUp() = repo.adjustDriverTemp(+0.5f)
    fun driverTempDown() = repo.adjustDriverTemp(-0.5f)
    fun passengerTempUp() = repo.adjustPassengerTemp(+0.5f)
    fun passengerTempDown() = repo.adjustPassengerTemp(-0.5f)
    fun cycleFan() = repo.cycleFan()
    fun toggleAuto() = repo.toggleAuto()
    fun toggleSync() = repo.toggleSync()
    fun toggleFrontDefrost() = repo.toggleFrontDefrost()
    fun toggleRearDefrost() = repo.toggleRearDefrost()
    fun cycleDriverSeatHeat() = repo.cycleDriverSeatHeat()
    fun cyclePassengerSeatHeat() = repo.cyclePassengerSeatHeat()
}
