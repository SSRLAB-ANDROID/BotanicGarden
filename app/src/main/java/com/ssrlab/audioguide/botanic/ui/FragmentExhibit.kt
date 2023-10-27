package com.ssrlab.audioguide.botanic.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.ssrlab.audioguide.botanic.MainActivity
import com.ssrlab.audioguide.botanic.R
import com.ssrlab.audioguide.botanic.client.ExhibitClient
import com.ssrlab.audioguide.botanic.databinding.FragmentExhibitBinding
import com.ssrlab.audioguide.botanic.rv.tab.TabExhibitAdapter
import com.ssrlab.audioguide.botanic.utils.BotanicMediaPlayer
import com.ssrlab.audioguide.botanic.utils.SWITCH_EXHIBIT_TIMER
import com.ssrlab.audioguide.botanic.vm.ExhibitViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class FragmentExhibit: Fragment() {

    private lateinit var mainActivity: MainActivity

    private lateinit var binding: FragmentExhibitBinding
    private lateinit var tabAdapter: TabExhibitAdapter

    private val viewModel: ExhibitViewModel by activityViewModels()
    private val scope = CoroutineScope(Dispatchers.Main)

    private lateinit var window: PopupWindow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivity = activity as MainActivity
        window = PopupWindow(mainActivity)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentExhibitBinding.inflate(layoutInflater)

        updateExhibit()

        binding.exhibitBack.setOnClickListener { mainActivity.onBackPressedDispatcher.onBackPressed() }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        setUpIdObserver()
        setUpOrderAction()
        setUpVolumeButton()
        setUpSpeedList()

        binding.exhibitButtonShow.setOnClickListener {
            viewModel.setPoint(1)
            mainActivity.getNavController().navigate(R.id.map_graph)
        }
    }

    override fun onStop() {
        super.onStop()

        BotanicMediaPlayer.pauseAudio(binding)
    }

    private fun setUpIdObserver() {
        viewModel.id.observe(this) {
            when (it) {
                0 -> {
                    binding.apply {
                        exhibitNextIc.setImageResource(R.drawable.ic_next_selector)
                        exhibitNextIc.isClickable = true
                        exhibitNextIc.isFocusable = true

                        exhibitPreviousIc.setImageResource(R.drawable.ic_previous_disabled)
                        exhibitPreviousIc.isClickable = false
                        exhibitPreviousIc.isFocusable = false
                    }
                }

                viewModel.getList().size - 1 -> {
                    binding.apply {
                        exhibitPreviousIc.setImageResource(R.drawable.ic_previous_selector)
                        exhibitPreviousIc.isClickable = true
                        exhibitPreviousIc.isFocusable = true

                        exhibitNextIc.setImageResource(R.drawable.ic_next_disabled)
                        exhibitNextIc.isClickable = false
                        exhibitNextIc.isFocusable = false
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
                    scope.launch {
                        delay(SWITCH_EXHIBIT_TIMER)

                        viewModel.id.value = viewModel.id.value?.minus(1)
                        BotanicMediaPlayer.pauseAudio(binding)
                        updateExhibit()
                    }
                }
            }

            exhibitNextIc.setOnClickListener {
                if (viewModel.id.value != viewModel.getList().size - 1) {
                    scope.launch {
                        delay(SWITCH_EXHIBIT_TIMER)

                        viewModel.id.value = viewModel.id.value?.plus(1)
                        BotanicMediaPlayer.pauseAudio(binding)
                        updateExhibit()
                    }
                }
            }
        }
    }

    private fun updateExhibit() {
        viewModel.apply {
            setChosenItem(getList()[id.value!!])
        }

        if (window.isShowing) window.dismiss()

        val imagesArray = arrayListOf<String>()
        for (i in viewModel.getExhibitObject().images.keys) viewModel.getExhibitObject().images[i]
            ?.let { imagesArray.add(it) }

        tabAdapter = TabExhibitAdapter(activity as MainActivity, imagesArray, viewModel)

        checkAudioAvailability()

        binding.apply {
            exhibitPager.adapter = tabAdapter

            TabLayoutMediator(binding.exhibitTabLayout, binding.exhibitPager) { _, _ -> }.attach()

            exhibitInfoText.text = viewModel.getExhibitObject().audioText
            exhibitTitle.text = viewModel.getExhibitObject().placeName
        }
    }

    private fun setUpVolumeButton() {
        if (!viewModel.isVolumeOn.value!!) binding.exhibitVolumeIc.setImageResource(R.drawable.ic_volume_off_selector)
        else binding.exhibitVolumeIc.setImageResource(R.drawable.ic_volume_on_selector)

        viewModel.isVolumeOn.observe(this) {
            if (!it) binding.exhibitVolumeIc.setImageResource(R.drawable.ic_volume_off_selector)
            else binding.exhibitVolumeIc.setImageResource(R.drawable.ic_volume_on_selector)
        }

        binding.exhibitVolumeIc.setOnClickListener {
            if (viewModel.isVolumeOn.value!!) mainActivity.controlVolume(0)
            else mainActivity.controlVolume(10)
        }
    }

    @SuppressLint("InflateParams")
    private fun setUpSpeedList() {
        binding.exhibitSpeedIc.setOnClickListener {

            if (window.isShowing) window.dismiss()
            else {
                val view = layoutInflater.inflate(R.layout.layout_popup, null)

                window.contentView = view
                val speed05 = view.findViewById<TextView>(R.id.speed_0_5)
                val speed075 = view.findViewById<TextView>(R.id.speed_0_75)
                val speed10 = view.findViewById<TextView>(R.id.speed_1_0)
                val speed125 = view.findViewById<TextView>(R.id.speed_1_25)
                val speed15 = view.findViewById<TextView>(R.id.speed_1_5)

                setSpeedAction(speed05, 0.5f)
                setSpeedAction(speed075, 0.75f)
                setSpeedAction(speed10, 1f)
                setSpeedAction(speed125, 1.25f)
                setSpeedAction(speed15, 1.5f)

                window.showAsDropDown(binding.exhibitSpeedIc)
            }
        }
    }

    private fun setSpeedAction(view: View, speed: Float) {
        view.setOnClickListener {
            BotanicMediaPlayer.changeAudioSpeed(speed)
            window.dismiss()
        }
    }

    private fun checkAudioAvailability() {
        val file = File(mainActivity.getExternalFilesDir(null), "botanical_${viewModel.getExhibitObject().placeId}_${mainActivity.getApp().getLocale()}.mp3")
        if (file.exists()) initMediaPlayer(file)
        else {
            if (viewModel.getExhibitObject().audio != "null") {
                ExhibitClient.getAudio(viewModel.getExhibitObject().audio, file, {
                    initMediaPlayer(file)
                }) { mainActivity.runOnUiThread { Toast.makeText(mainActivity, it, Toast.LENGTH_SHORT).show() } }
            } else {
                binding.exhibitPlayLoader.visibility = View.INVISIBLE
                binding.exhibitPlayIc.visibility = View.INVISIBLE
                binding.exhibitVolumeIc.visibility = View.INVISIBLE
                binding.exhibitSpeedIc.visibility = View.INVISIBLE
            }
        }
    }

    private fun initMediaPlayer(file: File) {
        binding.apply {
            mainActivity.runOnUiThread {
                exhibitPlayLoader.visibility = View.INVISIBLE
                exhibitPlayIc.visibility = View.VISIBLE
                exhibitVolumeIc.visibility = View.VISIBLE
                exhibitSpeedIc.visibility = View.VISIBLE
            }
        }

        BotanicMediaPlayer.initializeMediaPlayer(mainActivity, binding, file.toUri())
        binding.exhibitPlayIc.setOnClickListener { BotanicMediaPlayer.playAudio(mainActivity, binding) }
    }
}