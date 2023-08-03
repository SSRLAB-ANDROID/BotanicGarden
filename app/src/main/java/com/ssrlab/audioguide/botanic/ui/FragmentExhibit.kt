package com.ssrlab.audioguide.botanic.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.ssrlab.audioguide.botanic.MainActivity
import com.ssrlab.audioguide.botanic.R
import com.ssrlab.audioguide.botanic.databinding.FragmentExhibitBinding
import com.ssrlab.audioguide.botanic.rv.tab.TabExhibitAdapter
import com.ssrlab.audioguide.botanic.vm.ExhibitViewModel

class FragmentExhibit: Fragment() {

    private lateinit var binding: FragmentExhibitBinding
    private lateinit var tabAdapter: TabExhibitAdapter

    private val viewModel: ExhibitViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentExhibitBinding.inflate(layoutInflater)

        updateExhibit()

        binding.exhibitBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        setUpIdObserver()
        setUpOrderAction()
    }

    private fun setUpIdObserver() {
        viewModel.id.observe(this) {
            when (it) {
                0 -> {
                    binding.exhibitPreviousIc.apply {
                        setImageResource(R.drawable.ic_previous_disabled)
                        isClickable = false
                        isFocusable = false
                    }
                }

                viewModel.getList().size - 1 -> {
                    binding.exhibitNextIc.apply {
                        setImageResource(R.drawable.ic_next_disabled)
                        isClickable = false
                        isFocusable = false
                    }
                }

                else -> {
                    binding.apply {
                        exhibitNextIc.setImageResource(R.drawable.ic_next_selector)
                        exhibitNextIc.isClickable = true
                        exhibitNextIc.isFocusable = true

                        exhibitPreviousIc.setImageResource(R.drawable.ic_previous_selector)
                        exhibitPreviousIc.isClickable = true
                        exhibitPreviousIc.isFocusable = true
                    }
                }
            }
        }
    }

    private fun setUpOrderAction() {
        binding.apply {
            exhibitPreviousIc.setOnClickListener {
                if (viewModel.id.value != 0) {
                    viewModel.id.value = viewModel.id.value?.minus(1)
                    updateExhibit()
                }
            }

            exhibitNextIc.setOnClickListener {
                if (viewModel.id.value != viewModel.getList().size - 1) {
                    viewModel.id.value = viewModel.id.value?.plus(1)
                    updateExhibit()
                }
            }
        }
    }

    private fun updateExhibit() {
        viewModel.apply {
            setChosenItem(getList()[id.value!!])
        }

        val imagesArray = arrayListOf<String>()
        for (i in viewModel.getExhibitObject().images.keys) viewModel.getExhibitObject().images[i]
            ?.let { imagesArray.add(it) }

        tabAdapter = TabExhibitAdapter(activity as MainActivity, imagesArray)

        binding.apply {
            exhibitPager.adapter = tabAdapter

            TabLayoutMediator(binding.exhibitTabLayout, binding.exhibitPager) { _, _ -> }.attach()

            exhibitInfoText.text = viewModel.getExhibitObject().audioText
            exhibitTitle.text = viewModel.getExhibitObject().placeName
        }
    }

    private fun setUpVolumeButton() {

    }
}