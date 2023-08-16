package com.ssrlab.audioguide.botanic.rv.tab

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ssrlab.audioguide.botanic.vm.ExhibitViewModel

class TabExhibitAdapter(fragment: FragmentActivity, private val imageList: ArrayList<String>, private val vm: ExhibitViewModel): FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = imageList.size

    override fun createFragment(position: Int): Fragment {
        vm.setTabLink(imageList[position])
        return TabExhibitFragment()
    }
}