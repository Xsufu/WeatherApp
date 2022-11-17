package com.xolary.weathertraining.data

import android.widget.ImageView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest

object FormatData {
    // Функция перевода полученных погодных условий от API
    fun conditionTranslate(condition: String): String {
        val translatedCondition: String
        when (condition) {
            "clear" -> translatedCondition = "Ясно"
            "partly-cloudy" -> translatedCondition = "Малооблачно"
            "cloudy" -> translatedCondition = "Облачно с прояснениями"
            "overcast" -> translatedCondition = "Пасмурно"
            "drizzle " -> translatedCondition = "Морось"
            "light-rain " -> translatedCondition = "Небольшой дождь"
            "rain " -> translatedCondition = "Дождь"
            "moderate-rain" -> translatedCondition = "Умеренно сильный дождь"
            "heavy-rain" -> translatedCondition = "Сильный дождь"
            "continuous-heavy-rain" -> translatedCondition = "Длительный сильный дождь"
            "showers " -> translatedCondition = "Ливень"
            "wet-snow" -> translatedCondition = "Дождь со снегом"
            "light-snow" -> translatedCondition = "Небольшой снег"
            "snow" -> translatedCondition = "Снег"
            "snow-showers" -> translatedCondition = "Снегопад"
            "hail" -> translatedCondition = "Град"
            "thunderstorm" -> translatedCondition = "Гроза"
            "thunderstorm-with-rain" -> translatedCondition = "Дождь с грозой"
            "thunderstorm-with-hail" -> translatedCondition = "Гроза с градом"
            else -> translatedCondition = condition
        }
        return translatedCondition
    }

    fun ImageView.loadSvg(url: String) {
        val imageLoader = ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()

        val request = ImageRequest.Builder(this.context)
            .crossfade(true)
            .crossfade(300)
            .data(url)
            .target(this)
            .build()

        imageLoader.enqueue(request)
    }
}