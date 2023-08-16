package com.ssrlab.audioguide.botanic.rv.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import coil.load
import coil.transform.RoundedCornersTransformation
import com.ssrlab.audioguide.botanic.databinding.TabExhibitBinding
import com.ssrlab.audioguide.botanic.vm.ExhibitViewModel

class TabExhibitFragment : Fragment() {

    private lateinit var binding: TabExhibitBinding
    private val viewModel: ExhibitViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = TabExhibitBinding.inflate(layoutInflater)

        binding.tabImage.load(viewModel.getTabLink()) {
            crossfade(true)
            transformations(RoundedCornersTransformation(16f))
        }

        return binding.root
    }
}