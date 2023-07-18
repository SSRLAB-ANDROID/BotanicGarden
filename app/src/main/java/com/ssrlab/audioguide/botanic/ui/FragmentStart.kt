package com.ssrlab.audioguide.botanic.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ssrlab.audioguide.botanic.databinding.FragmentStartScreenBinding

class FragmentStart: Fragment() {

    private lateinit var binding: FragmentStartScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentStartScreenBinding.inflate(layoutInflater)

        return binding.root
    }
}