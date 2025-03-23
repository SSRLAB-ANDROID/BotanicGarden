package com.ssrlab.audioguide.botanic.ui

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.ssrlab.audioguide.botanic.MainActivity
import com.ssrlab.audioguide.botanic.R
import com.ssrlab.audioguide.botanic.client.ExhibitClient
import com.ssrlab.audioguide.botanic.databinding.FragmentExhibitBinding
import com.ssrlab.audioguide.botanic.rv.tab.TabExhibitAdapter
import com.ssrlab.audioguide.botanic.utils.BotanicMediaPlayer
import com.ssrlab.audioguide.botanic.vm.ExhibitViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class FragmentExhibit : Fragment() {

    private lateinit var mainActivity: MainActivity

    private lateinit var binding: FragmentExhibitBinding
    private lateinit var tabAdapter: TabExhibitAdapter

    private val viewModel: ExhibitViewModel by activityViewModels()
    private val scope = CoroutineScope(Dispatchers.Main)

    private lateinit var window: PopupWindow

    private var isAudioAvailable = false

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
        binding.exhibitBack.setOnClickListener { mainActivity.onBackPressedDispatcher.onBackPressed() }

        viewModel.updateExhibit(window, binding) {
            tabAdapter = TabExhibitAdapter(activity as MainActivity, it, viewModel)
            binding.exhibitPager.adapter = tabAdapter

            isAudioAvailable = checkAudioAvailability()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        viewModel.setUpMoveActions(binding, this@FragmentExhibit)
        viewModel.setUpOrderAction(binding, scope) {
            viewModel.updateExhibit(window, binding) {
                tabAdapter = TabExhibitAdapter(activity as MainActivity, it, viewModel)
                binding.exhibitPager.adapter = tabAdapter

                isAudioAvailable = checkAudioAvailability()
            }
        }
        if (isAudioAvailable) {
            val file = getAudio()
            if (file.exists()) {
                if (file.length() == 0L) checkAudioAction(file)
                else {
                    initMediaPlayer(file)
                }
            }
        }
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

        if (window.isShowing) window.dismiss()
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

    private fun checkAudioAvailability(): Boolean {
        val file = getAudio()
        if (file.exists()) {
            if (file.length() == 0L) checkAudioAction(file)
            else {
                initMediaPlayer(file)
                return true
            }
        } else {
            if (viewModel.getExhibitObject().audio != "null") checkAudioAction(file)
            else {
                binding.exhibitDurationHolder.visibility = View.GONE
                binding.exhibitPlayLoader.visibility = View.INVISIBLE
                binding.exhibitPlayIc.visibility = View.INVISIBLE
                binding.exhibitVolumeIc.visibility = View.INVISIBLE
                binding.exhibitSpeedIc.visibility = View.INVISIBLE
            }
        }
        return false
    }

    private fun checkAudioAction(file: File) {
        ExhibitClient.getAudio(viewModel.getExhibitObject().audio, file, {
            initMediaPlayer(file)
        }) {
            mainActivity.runOnUiThread {
                Toast.makeText(mainActivity, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initMediaPlayer(file: File) {
        BotanicMediaPlayer.initializeMediaPlayer(mainActivity, binding, requireContext(), file) {
            binding.apply {
                scope.launch {
                    delay(100)
                    exhibitPlayLoader.visibility = View.INVISIBLE
                    exhibitDurationHolder.visibility = View.VISIBLE
                    exhibitPlayIc.visibility = View.VISIBLE
                    exhibitVolumeIc.visibility = View.VISIBLE
                    exhibitSpeedIc.visibility = View.VISIBLE
                    exhibitPlayIc.setOnClickListener {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            BotanicMediaPlayer.playAudio(mainActivity, binding, requireContext())
                        }
                    }
                }
            }
        }
    }

    private fun getAudio(): File {
        return File(
            mainActivity.getExternalFilesDir(null),
            "botanical_${viewModel.getExhibitObject().placeId}_${
                mainActivity.getApp().getLocale()
            }.mp3"
        )
    }
}