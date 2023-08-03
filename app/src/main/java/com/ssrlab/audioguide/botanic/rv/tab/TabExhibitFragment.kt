package com.ssrlab.audioguide.botanic.rv.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import coil.load
import coil.transform.RoundedCornersTransformation
import com.ssrlab.audioguide.botanic.databinding.TabExhibitBinding

class TabExhibitFragment(private val link: String): Fragment() {

    private lateinit var binding: TabExhibitBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = TabExhibitBinding.inflate(layoutInflater)

        binding.tabImage.load(link) {
            crossfade(true)
            transformations(RoundedCornersTransformation(16f))
        }

        return binding.root
    }
}