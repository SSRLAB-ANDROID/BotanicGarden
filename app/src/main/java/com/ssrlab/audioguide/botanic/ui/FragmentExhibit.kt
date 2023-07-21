package com.ssrlab.audioguide.botanic.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.ssrlab.audioguide.botanic.MainActivity
import com.ssrlab.audioguide.botanic.databinding.FragmentExhibitBinding
import com.ssrlab.audioguide.botanic.rv.tab.TabExhibitAdapter

class FragmentExhibit: Fragment() {

    private lateinit var binding: FragmentExhibitBinding
    private lateinit var tabAdapter: TabExhibitAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentExhibitBinding.inflate(layoutInflater)

        tabAdapter = TabExhibitAdapter(activity as MainActivity, arrayListOf("", "", "", ""))
        binding.exhibitPager.adapter = tabAdapter

        TabLayoutMediator(binding.exhibitTabLayout, binding.exhibitPager) { tab, position ->

        }.attach()

        return binding.root
    }
}