package com.xolary.weathertraining.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xolary.weathertraining.R
import com.xolary.weathertraining.data.FormatData.loadSvg
import com.xolary.weathertraining.data.WeatherData
import com.xolary.weathertraining.databinding.ListItemFragmentBinding

// Адаптер, который работает и ладно. Обработка, сохранение списка, сравнение элементов
class WeatherAdapter(private val listener: Listener?) : ListAdapter<WeatherData, WeatherAdapter.Holder>(DiffCallback()) {

    // Логика заполнения View
    class Holder(view: View, private val listener: Listener?) : RecyclerView.ViewHolder(view) {
        val binding = ListItemFragmentBinding.bind(view)
        var itemTemp: WeatherData? = null

        init {
            itemView.setOnClickListener {
                itemTemp?.let { it1 -> listener?.onClick(it1) }
            }
        }

        // Вывод данных на экран
        fun bind(item: WeatherData) = with(binding) {
            val minMaxTemp = "${item.minTemp}° ${item.maxTemp}°"
            val currentTemp = "${item.currentTemp}°"
            itemTemp = item
            tvCardDate.text = item.time
            tvCardCondition.text = item.condition
            tvCardTemp.text = if (item.currentTemp.isEmpty()) minMaxTemp else currentTemp

            val imgURL = "https://yastatic.net/weather/i/icons/funky/dark/${item.imageUrl}.svg"
            ivCardCondition.loadSvg(imgURL)
        }
    }

    // Загрузка View в память
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherAdapter.Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_fragment, parent, false)

        return WeatherAdapter.Holder(view, listener)
    }

    // Добавление данных с текущей позиции
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    // Сравнение элементов при заполнении
    class DiffCallback : DiffUtil.ItemCallback<WeatherData>() {
        override fun areItemsTheSame(oldItem: WeatherData, newItem: WeatherData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: WeatherData, newItem: WeatherData): Boolean {
            return oldItem == newItem
        }

    }

    interface Listener {
        fun onClick(item: WeatherData)
    }
}