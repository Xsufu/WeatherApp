package com.xolary.weathertraining.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(
    fragmentAdapter: FragmentActivity,
    private val fragmentList: List<Fragment>
) : FragmentStateAdapter(fragmentAdapter) {

    // Возвращает количество переданных фрагментов
    override fun getItemCount(): Int {
        return fragmentList.size
    }

    // Возвращает выбранный фрагмент
    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}