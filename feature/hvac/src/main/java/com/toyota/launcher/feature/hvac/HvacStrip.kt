package com.toyota.launcher.feature.hvac

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AirlineSeatReclineNormal
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toyota.launcher.core.design.LocalSpacing
import com.toyota.launcher.core.design.ToyotaColors
import kotlin.math.roundToInt

/**
 * Bottom HVAC strip. Mirrors the reference layout:
 *
 *   [drvTemp 68]  [drvSeat]  [drvDefr]  [AUTO]  [fanDown]  [fan]  [fanUp]
 *   [carIcon SYNC]  [REAR68]  [paxSeat]  [paxTemp 68]
 */
@Composable
fun HvacStrip(modifier: Modifier = Modifier) {
    val vm: HvacViewModel = hiltViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    HvacStripContent(
        state = state,
        onDriverDown = vm::driverTempDown,
        onDriverUp = vm::driverTempUp,
        onPassengerDown = vm::passengerTempDown,
        onPassengerUp = vm::passengerTempUp,
        onFanCycle = vm::cycleFan,
        onAuto = vm::toggleAuto,
        onSync = vm::toggleSync,
        onFrontDefrost = vm::toggleFrontDefrost,
        onRearDefrost = vm::toggleRearDefrost,
        onDriverSeat = vm::cycleDriverSeatHeat,
        onPassengerSeat = vm::cyclePassengerSeatHeat,
        modifier = modifier,
    )
}

@Composable
internal fun HvacStripContent(
    state: HvacState,
    onDriverDown: () -> Unit,
    onDriverUp: () -> Unit,
    onPassengerDown: () -> Unit,
    onPassengerUp: () -> Unit,
    onFanCycle: () -> Unit,
    onAuto: () -> Unit,
    onSync: () -> Unit,
    onFrontDefrost: () -> Unit,
    onRearDefrost: () -> Unit,
    onDriverSeat: () -> Unit,
    onPassengerSeat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalSpacing.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ToyotaColors.HvacBg)
            .padding(horizontal = s.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // LEFT: driver temp + driver seat heat + front defrost + AUTO
        Row(verticalAlignment = Alignment.CenterVertically) {
            TempControl(
                temp = formatTemp(state.driverTempC, state.displayUnitFahrenheit),
                onDown = onDriverDown,
                onUp = onDriverUp,
            )
            Spacer(Modifier.width(s.md))
            ChipIcon(
                icon = Icons.Filled.AirlineSeatReclineNormal,
                active = state.driverSeatHeatLevel > 0,
                onClick = onDriverSeat,
                tint = ToyotaColors.HvacAccentRed,
            )
            Spacer(Modifier.width(s.sm))
            ChipIcon(
                icon = Icons.Filled.WbSunny,
                active = state.defrosterFront,
                onClick = onFrontDefrost,
                tint = ToyotaColors.HvacAccentRed,
            )
        }

        // CENTER: AUTO + fan controls
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextChip(
                text = "AUTO",
                active = state.autoOn,
                onClick = onAuto,
            )
            Spacer(Modifier.width(s.sm))
            FanControl(
                level = state.fanSpeed,
                max = state.fanSpeedMax,
                onCycle = onFanCycle,
            )
        }

        // RIGHT: car/sync + rear defrost + passenger seat + passenger temp
        Row(verticalAlignment = Alignment.CenterVertically) {
            CarSyncChip(active = state.syncOn, onClick = onSync)
            Spacer(Modifier.width(s.sm))
            RearDefrostChip(
                tempLabel = formatTemp(state.passengerTempC, state.displayUnitFahrenheit),
                active = state.defrosterRear,
                onClick = onRearDefrost,
            )
            Spacer(Modifier.width(s.sm))
            ChipIcon(
                icon = Icons.Filled.AirlineSeatReclineNormal,
                active = state.passengerSeatHeatLevel > 0,
                onClick = onPassengerSeat,
                tint = ToyotaColors.HvacAccentRed,
            )
            Spacer(Modifier.width(s.md))
            TempControl(
                temp = formatTemp(state.passengerTempC, state.displayUnitFahrenheit),
                onDown = onPassengerDown,
                onUp = onPassengerUp,
            )
        }
    }
}

@Composable
private fun TempControl(temp: String, onDown: () -> Unit, onUp: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ChipIcon(icon = Icons.Filled.SwapHoriz, active = false, onClick = onDown, rotateDeg = 180f)
        Text(
            text = temp,
            color = ToyotaColors.HvacOn,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 6.dp),
        )
        ChipIcon(icon = Icons.Filled.SwapHoriz, active = false, onClick = onUp)
    }
}

@Composable
private fun FanControl(level: Int, max: Int, onCycle: () -> Unit) {
    val s = LocalSpacing.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ToyotaColors.SurfaceVariant)
            .clickable { onCycle() }
            .padding(horizontal = s.sm, vertical = 6.dp),
    ) {
        Icon(Icons.Filled.Air, null, tint = ToyotaColors.HvacOn, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        // Toyota-like 7-bar indicator
        repeat(max) { i ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 1.dp)
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(if (i < level) ToyotaColors.HvacOn else ToyotaColors.OutlineSubtle),
            )
        }
        Spacer(Modifier.width(6.dp))
        Icon(Icons.Filled.Air, null, tint = ToyotaColors.HvacOn, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun TextChip(text: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) ToyotaColors.HvacSyncOn else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            color = ToyotaColors.HvacOn,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun ChipIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    tint: Color = ToyotaColors.HvacOn,
    rotateDeg: Float = 0f,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) tint.copy(alpha = 0.25f) else Color.Transparent)
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) tint else ToyotaColors.HvacOn,
            modifier = Modifier
                .size(22.dp)
                .let { if (rotateDeg != 0f) it.background(Color.Transparent) else it },
        )
    }
}

@Composable
private fun CarSyncChip(active: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) ToyotaColors.HvacSyncOn else ToyotaColors.SurfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.DirectionsCar, null,
            tint = ToyotaColors.HvacOn,
            modifier = Modifier.size(20.dp),
        )
        Text(
            "SYNC",
            color = ToyotaColors.HvacOn,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun RearDefrostChip(tempLabel: String, active: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) ToyotaColors.HvacAccentRed.copy(alpha = 0.25f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.NorthEast, null,
            tint = if (active) ToyotaColors.HvacAccentRed else ToyotaColors.HvacOn,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(4.dp))
        Column {
            Text(
                "REAR",
                color = ToyotaColors.OnSurfaceMuted,
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                tempLabel,
                color = ToyotaColors.HvacOn,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

private fun formatTemp(celsius: Float, fahrenheit: Boolean): String {
    val v = if (fahrenheit) celsius * 9f / 5f + 32f else celsius
    return "${v.roundToInt()}\u00B0"
}
