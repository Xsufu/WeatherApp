package com.xolary.weathertraining.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.xolary.weathertraining.MainViewModel
import com.xolary.weathertraining.adapters.WeatherAdapter
import com.xolary.weathertraining.data.FormatData.conditionTranslate
import com.xolary.weathertraining.data.WeatherData
import com.xolary.weathertraining.databinding.FragmentHoursBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HoursFragment : Fragment() {

    private lateinit var binding: FragmentHoursBinding
    private lateinit var adapter: WeatherAdapter
    private val viewModel: MainViewModel by activityViewModels()
    private val simpleDateFormat = SimpleDateFormat("HH:mm")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHoursBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewInit()
        viewModel.liveDataCurrent.observe(viewLifecycleOwner) {
            if (getHoursList(it).isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
            } else {
                binding.tvEmpty.visibility = View.INVISIBLE
            }
            adapter.submitList(getHoursList(it))
        }
    }

    // Инициализация RecyclerView
    private fun recyclerViewInit() = with(binding) {
        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = WeatherAdapter(null)
        recyclerView.adapter = adapter
    }

    // Получение почасовой погоды списком
    private fun getHoursList(weatherItem: WeatherData): List<WeatherData> {
        val hoursArray = JSONArray(weatherItem.hours)
        val weatherList = ArrayList<WeatherData>()

        for (i in 0 until hoursArray.length()) {
            val item = WeatherData(
                "",
                simpleDateFormat.format(Date((hoursArray[i] as JSONObject).getString("hour_ts").toLong() * 1000)),
                conditionTranslate((hoursArray[i] as JSONObject).getString("condition")),
                (hoursArray[i] as JSONObject).getString("icon"),
                (hoursArray[i] as JSONObject).getString("temp"),
                "",
                "",
                ""
            )
            weatherList.add(item)
        }
        return weatherList
    }

    // Загрузка SVG изображения через Coil
    private fun ImageView.loadSvg(url: String) {
        val imageLoader = ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()

        val request = ImageRequest.Builder(this.context)
            .crossfade(true)
            .crossfade(500)
            .data(url)
            .target(this)
            .build()

        imageLoader.enqueue(request)
    }

    companion object {
        @JvmStatic
        fun newInstance() = HoursFragment()
    }
}