package com.toyota.launcher.feature.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toyota.launcher.core.design.BigCard
import com.toyota.launcher.core.design.LocalSpacing
import com.toyota.launcher.core.design.ToyotaColors

@Composable
fun WeatherCard(modifier: Modifier = Modifier) {
    val vm: WeatherViewModel = hiltViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    WeatherCardContent(state = state, modifier = modifier)
}

@Composable
internal fun WeatherCardContent(state: WeatherState, modifier: Modifier = Modifier) {
    val s = LocalSpacing.current
    BigCard(backgroundColor = ToyotaColors.WeatherBg, modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocationOn, null,
                    tint = ToyotaColors.WeatherOnBg,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = state.location,
                    color = ToyotaColors.WeatherOnBg,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }

            Box(
                modifier = Modifier
                    .padding(vertical = s.sm)
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(ToyotaColors.WeatherSun),
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${state.tempF}\u00B0",
                    color = ToyotaColors.WeatherOnBg,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Light,
                    ),
                )
                Text(
                    text = state.condition,
                    color = ToyotaColors.WeatherOnBg,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.LocationOn, null,
                    tint = ToyotaColors.WeatherOnBg.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = state.attribution,
                    color = ToyotaColors.WeatherOnBg.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
    }
}
