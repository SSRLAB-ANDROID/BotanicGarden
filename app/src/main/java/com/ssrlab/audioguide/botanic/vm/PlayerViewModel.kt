package com.ssrlab.audioguide.botanic.vm

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.ssrlab.audioguide.botanic.MainActivity
import com.ssrlab.audioguide.botanic.R
import com.ssrlab.audioguide.botanic.databinding.FragmentExhibitBinding
import com.ssrlab.audioguide.botanic.utils.HelpFunctions
import kotlinx.coroutines.*
import java.io.IOException

class PlayerViewModel: ViewModel() {

    private var mpStatus = "play"

    private val mediaScope = CoroutineScope(Dispatchers.IO)
    private var mediaPlayer: MediaPlayer? = null
    private var speed: Float? = null

    private var binding: FragmentExhibitBinding? = null
    private val helpFunctions = HelpFunctions()

    fun initializeMediaPlayer(uri: String, binding: FragmentExhibitBinding, activity: MainActivity) {

        if (uri != "null") {

            binding.apply {
                exhibitPlayIc.visibility = View.VISIBLE
                exhibitVolumeIc.visibility = View.VISIBLE
                exhibitSpeedIc.visibility = View.VISIBLE
            }

            mpStatus = "play"

            mediaPlayer = MediaPlayer()

            try {
                mediaPlayer!!.setDataSource(uri)
                if (speed != null) {
                    val playBackParams = PlaybackParams()
                    playBackParams.speed = speed!!
                    mediaPlayer!!.playbackParams = playBackParams
                }
                mediaPlayer!!.prepare()

                this.binding = binding

                binding.apply {
                    exhibitDurationBar.max = mediaPlayer!!.duration
                    exhibitDurationBar.progress = 0
                    exhibitDurationTime.text = helpFunctions.convertToTimerMode(mediaPlayer!!.duration)
                    exhibitCurrentTime.text = helpFunctions.convertToTimerMode(mediaPlayer!!.currentPosition)
                    exhibitPlayIc.setImageResource(R.drawable.ic_play_selector)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            listenProgress(mediaPlayer!!, activity)
        } else binding.apply {
            exhibitPlayIc.visibility = View.INVISIBLE
            exhibitVolumeIc.visibility = View.INVISIBLE
            exhibitSpeedIc.visibility = View.INVISIBLE
        }
    }

    fun playAudio(context: Context, binding: FragmentExhibitBinding, activity: MainActivity) {
        mediaScope.launch {
            when (mpStatus) {

                "pause" -> {
                    mediaPlayer!!.pause()
                    mpStatus = "continue"
                    binding.exhibitPlayIc.setImageResource(R.drawable.ic_play_selector)
                }
                "continue" -> {
                    mediaPlayer!!.start()
                    mpStatus = "pause"
                    mediaScope.launch { initProgressListener(mediaPlayer!!, binding, activity) }
                    binding.exhibitPlayIc.setImageResource(R.drawable.ic_pause_selector)
                }
                "play" -> {
                    try {
                        mediaPlayer!!.start()
                        mediaScope.launch { initProgressListener(mediaPlayer!!, binding, activity) }
                        mpStatus = "pause"
                        binding.exhibitPlayIc.setImageResource(R.drawable.ic_pause_selector)
                    } catch (e: Exception){
                        Toast.makeText(context, "Player error: ${e.message.toString()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun changeAudioSpeed(speed: Float) {
        val playBackParams = PlaybackParams()
        playBackParams.speed = speed
        this.speed = speed
        mediaPlayer?.playbackParams = playBackParams
    }

    fun mpPause(binding: FragmentExhibitBinding) {
        mpStatus = "continue"
        mediaPlayer?.pause()
        binding.exhibitPlayIc.setImageResource(R.drawable.ic_play_selector)
    }

    fun mpStop() {

        if (mpStatus != "play") {

            mpStatus = "stop"

            if (mediaPlayer != null) {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer!!.stop()
                    mediaPlayer!!.release()
                    mediaPlayer = null
                } else mediaPlayer?.release()
            }
        } else if (mpStatus == "stop") { }
    }

    private suspend fun initProgressListener(mediaPlayer: MediaPlayer?, binding: FragmentExhibitBinding, activity: MainActivity) {
        while (mpStatus == "pause") {
            activity.runOnUiThread {
                binding.apply {
                    if (mediaPlayer?.isPlaying == true) {
                        exhibitCurrentTime.text = mediaPlayer?.currentPosition?.let {
                            helpFunctions.convertToTimerMode(it)
                        }
                        exhibitDurationBar.progress = mediaPlayer?.currentPosition!!
                    }
                }
            }
            delay(50)

            binding.exhibitDurationBar.apply {
                if (progress == max) {
                    mpStatus = "play"
                    delay(50)

                    mediaPlayer?.seekTo(0)
                    activity.runOnUiThread {
                        binding.apply {
                            exhibitPlayIc.setImageResource(R.drawable.ic_play_selector)
                            exhibitDurationBar.progress = 0
                            exhibitCurrentTime.text = mediaPlayer?.currentPosition?.let {
                                helpFunctions.convertToTimerMode(it)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun listenProgress(mediaPlayer: MediaPlayer, activity: MainActivity) {
        binding!!.exhibitDurationBar.setOnSeekBarChangeListener(
            helpFunctions.createSeekBarProgressListener {
                mediaPlayer.seekTo(it)
                activity.runOnUiThread {
                    binding!!.exhibitCurrentTime.text = helpFunctions.convertToTimerMode(mediaPlayer.currentPosition)
                }
            }
        )
    }

    override fun onCleared() {
        super.onCleared()

        mpStop()
    }
}