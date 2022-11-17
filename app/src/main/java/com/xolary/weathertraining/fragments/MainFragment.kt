package com.xolary.weathertraining.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.xolary.weathertraining.DialogManager
import com.xolary.weathertraining.MainViewModel
import com.xolary.weathertraining.adapters.ViewPagerAdapter
import com.xolary.weathertraining.data.FormatData.conditionTranslate
import com.xolary.weathertraining.data.FormatData.loadSvg
import com.xolary.weathertraining.data.WeatherData
import com.xolary.weathertraining.databinding.FragmentMainBinding
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

const val API_KEY = "b9e37ee9-b194-487e-a60e-3193e5186417"

class MainFragment : Fragment() {

    private lateinit var pLauncher: ActivityResultLauncher<String>
    private lateinit var binding: FragmentMainBinding
    private val viewModel: MainViewModel by activityViewModels()
    private val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy")
    private lateinit var fLocationClient: FusedLocationProviderClient
    private lateinit var cityName: String

    // Список фрагментов для переключения
    private val fragmentsList = listOf(
        DaysFragment.newInstance(),
        HoursFragment.newInstance()
    )


    // Список заголовков для TabLayout
    private val tabHeadersList = listOf("По дням", "По часам")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Запускаем проверку и запрос разрешения после инициализации фрагмента
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startAnimation()        // Анимация загрузки
        checkPermission()       // Проверка разрешений
        adapterInit()           // Инициализация адаптера
        getLocation()           // Получение местоположения
        updateCurrentWeather()  // Обновление текущей погоды
    }

    override fun onResume() {
        super.onResume()
        checkLocation()
    }

    // Запуск анимации загрузки
    private fun startAnimation() {
        val anim = RotateAnimation(
            0.0f,
            360.0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        anim.interpolator = LinearInterpolator()
        anim.repeatCount = Animation.INFINITE
        anim.duration = 700

        binding.ivWeather.startAnimation(anim)
    }

    // Прекращение анимации загрузки
    private fun stopAnimation() {
        binding.ivWeather.animation = null
    }

    // Проверка состояния активности геолокации
    private fun isLocationEnabled(): Boolean {
        val locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /**
     * Получение координат по названию города
     *
     * @param city название города
     *
     * @return список с широтой (0) и долготой(1)
     */
    private fun getCoordinatesByCity(city: String): List<Double> {
        return try {
            val resultList = ArrayList<Double>()
            val dataList = Geocoder(context).getFromLocationName(city,1)

            resultList.add(dataList[0].latitude)
            resultList.add(dataList[0].longitude)

            resultList
        } catch (e: java.lang.IndexOutOfBoundsException) {
            Toast.makeText(context, "Город не найден", Toast.LENGTH_SHORT).show()
            listOf(55.755864, 37.617698)
        }
    }

    /**
     * Получение названия города по координатам через Google Maps
     *
     * @param lat широта
     * @param lon долгота
     */
    private fun getCityName(lat: Double, lon: Double): String {
        val address = Geocoder(context).getFromLocation(lat, lon, 1)
        val city = if (address[0].locality == null) {
            if (address[0].adminArea == null) {
                address[0].countryName
            } else {
                address[0].adminArea
            }
        } else {
            address[0].locality
        }

        return city
    }

    // Привязка адаптера к ViewPager
    private fun adapterInit() = with(binding) {
        fLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val adapter = ViewPagerAdapter(activity as FragmentActivity, fragmentsList)
        viewPager.adapter = adapter

        // Добавление заголовков в TabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
            tab.text = tabHeadersList[pos]
        }.attach()
        ibSync.setOnClickListener {
            checkLocation()
        }
        ibSearch.setOnClickListener {
            DialogManager.searchByCityName(requireContext(), object : DialogManager.Listener {
                override fun onClick(newCityName: String?) {
                    var coordinates = listOf(55.755864, 37.617698)
                    if (newCityName != null) {
                        coordinates = getCoordinatesByCity(newCityName)
                        cityName = getCityName(coordinates[0], coordinates[1])
                    } else {
                        cityName = "Moscow"
                    }
                    requestWeatherDataYandex(coordinates[0], coordinates[1])
                }
            })
        }
    }

    // функция получения геолокации
    private fun getLocation() {
        val cancellationToken = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationToken.token)
            .addOnCompleteListener {
                // Запрос на получение погоды
                requestWeatherDataYandex(it.result.latitude, it.result.longitude)
                // Установка названия города по координатам
                cityName = getCityName(it.result.latitude, it.result.longitude)
            }
    }

    // Функция проверки включенности геолокации
    // Если локация включена - вызов функции её получения
    // Иначе - вызов диалога с предложением включить геолокацию
    private fun checkLocation() {
        if (isLocationEnabled()) {
            getLocation()
        } else {
            DialogManager.locationSettingsDialog(requireContext(), object : DialogManager.Listener {
                override fun onClick(newCityName: String?) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
        }
    }

    private fun updateCurrentWeather() = with(binding) {
        viewModel.liveDataCurrent.observe(viewLifecycleOwner) {
            val minMaxTemp = "${it.minTemp}° ${it.maxTemp}°"
            val currentTemp = "${it.currentTemp}°"
            tvDateTime.text = it.time
            tvCity.text = it.city
            tvCurrTemp.text = if(it.currentTemp.isEmpty()) minMaxTemp else currentTemp
            tvCondition.text = it.condition
            tvMinMax.text = if (it.currentTemp.isEmpty()) "" else minMaxTemp

            val imgURL = "https://yastatic.net/weather/i/icons/funky/dark/${it.imageUrl}.svg"
            ivWeather.loadSvg(imgURL)
            stopAnimation()
        }
    }

    // Запрос разрешения
    private fun permissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }
    }

    // Проверка разрешения
    // Если разрешения нет - запуск запроса на разрешение использования местоположения
    private fun checkPermission() {
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Запрос на получение данных с API Yandex
    private fun requestWeatherDataYandex(latitude: Double, longitude: Double) {
        val BASE_URL =
            "https://api.weather.yandex.ru/v2/forecast?" +
                    "lat=${latitude}" +
                    "&lon=${longitude}" +
                    "&lang=ru_RU" +
                    "&limit=7" +
                    "&hours=true"

        val queue = Volley.newRequestQueue(context)
        val request = object : StringRequest(
            Method.GET,
            BASE_URL,
            { result -> parseWeatherData(result) },
            { error -> Log.d("APILog", "Error: $error") }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["X-Yandex-API-Key"] = API_KEY
                return headers
            }
        }
        queue.add(request)
    }

    // Полуение данных с API в виде класса WeatherData
    private fun parseWeatherData(result: String) {
        val mainObject = JSONObject(result)
        val forecastYandex = parseYandexForecast(mainObject)
        parseYandexCurrent(mainObject, forecastYandex[0])
    }

    // Яндекс
    private fun parseYandexCurrent(mainObject: JSONObject, weatherItem: WeatherData) {
        val item = WeatherData(
            cityName,
            simpleDateFormat.format(Date(mainObject.getString("now").toLong() * 1000)),
            conditionTranslate(mainObject.getJSONObject("fact").getString("condition")),
            weatherItem.imageUrl,
            mainObject.getJSONObject("fact").getString("temp"),
            weatherItem.maxTemp,
            weatherItem.minTemp,
            weatherItem.hours
        )
        viewModel.liveDataCurrent.value = item
    }

    private fun parseYandexForecast(mainObject: JSONObject): List<WeatherData> {
        val weatherList = ArrayList<WeatherData>()
        val daysArray = mainObject.getJSONArray("forecasts")

        for (i in 0 until daysArray.length()) {
            val day = daysArray[i] as JSONObject
            val item = WeatherData(
                cityName,
                simpleDateFormat.format(Date(day.getString("date_ts").toLong() * 1000)),
                conditionTranslate(
                    day.getJSONObject("parts").getJSONObject("day").getString("condition")
                ),
                day.getJSONObject("parts").getJSONObject("day").getString("icon"),
                "",
                day.getJSONObject("parts").getJSONObject("day").getString("temp_max"),
                day.getJSONObject("parts").getJSONObject("day").getString("temp_min"),
                day.getJSONArray("hours").toString()
            )
            weatherList.add(item)
        }
        viewModel.liveDataList.value = weatherList
        return weatherList
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}