package com.xolary.weathertraining

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.xolary.weathertraining.data.WeatherData

class MainViewModel: ViewModel() {
    // Данные о текущей погоде
    val liveDataCurrent = MediatorLiveData<WeatherData>()

    // Список с прогнозом погоды
    val liveDataList = MediatorLiveData<List<WeatherData>>()
}