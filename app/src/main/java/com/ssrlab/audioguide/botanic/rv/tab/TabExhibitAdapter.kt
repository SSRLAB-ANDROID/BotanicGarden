package com.ssrlab.audioguide.botanic.rv.tab

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabExhibitAdapter(fragment: FragmentActivity, private val imageList: ArrayList<String>): FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = imageList.size

    override fun createFragment(position: Int): Fragment {
        return TabExhibitFragment(imageList[position])
    }
}