package com.toyota.launcher.feature.drivemode

import android.car.VehiclePropertyIds
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toyota.launcher.core.car.CarPropertyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DriveModeViewModel @Inject constructor(
    carProps: CarPropertyRepository,
) : ViewModel() {

    private val mode = MutableStateFlow(DriveMode.NORMAL)

    private val gear = carProps.sharedPropertyFlow<Int>(VehiclePropertyIds.GEAR_SELECTION)
        .onStart { emit(0) }

    val state: StateFlow<DriveModeUiState> =
        combine(mode, gear) { m, g -> DriveModeUiState(mode = m, gear = GearLabel.from(g)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DriveModeUiState())

    fun cycleMode() {
        val ordered = DriveMode.entries
        val nextIdx = (ordered.indexOf(mode.value) + 1) % ordered.size
        mode.value = ordered[nextIdx]
    }
}
