package com.xolary.weathertraining.data

import java.util.Date

// Набор данных, получаемых от API
data class WeatherData(
    val city: String,           // Город
    val time: String,           // Дата и время
    val condition: String,      // Погодные условия
    val imageUrl: String,       // Картинка погодных условий
    val currentTemp: String,    // Текущая температура
    val maxTemp: String,        // Максимальная температура
    val minTemp: String,        // Минимальная температура
    val hours: String           // Погода по часам в виде JSON строки
)
