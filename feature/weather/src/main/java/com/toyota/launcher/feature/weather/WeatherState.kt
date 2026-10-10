package com.toyota.launcher.feature.weather

data class WeatherState(
    val location: String = "Plano",
    val tempF: Int = 83,
    val tempC: Int = 28,
    val condition: String = "Sunny",
    val icon: String = "sun",
    val attribution: String = "AccuWeather",
)
