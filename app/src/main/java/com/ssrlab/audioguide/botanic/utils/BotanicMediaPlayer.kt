package com.ssrlab.audioguide.botanic.utils

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import com.ssrlab.audioguide.botanic.MainActivity
import com.ssrlab.audioguide.botanic.R
import com.ssrlab.audioguide.botanic.databinding.FragmentExhibitBinding
import kotlinx.coroutines.*
import java.io.File

object BotanicMediaPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private val helpFunctions = HelpFunctions()

    private var speed: Float? = null
    private var playerStatus = ""

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    fun initializeMediaPlayer(activity: MainActivity, binding: FragmentExhibitBinding, file: File, onSuccess: () -> Unit) {
        pauseAudio(binding)
        playerStatus = "play"
        mediaPlayer = MediaPlayer()

        val uri = file.toUri()

        activity.runOnUiThread {
            while (file.length() == 0L) {

                binding.apply {
                    exhibitPlayLoader.visibility = View.VISIBLE
                    exhibitPlayIc.visibility = View.INVISIBLE
                }
            }

            binding.apply {
                exhibitPlayLoader.visibility = View.INVISIBLE
                exhibitPlayIc.visibility = View.VISIBLE
            }
        }

        try {
            mediaPlayer!!.setDataSource(activity, uri)

            if (speed != null) {
                val playBackParams = PlaybackParams()
                playBackParams.speed = speed!!
                mediaPlayer!!.playbackParams = playBackParams
            }
            mediaPlayer!!.prepare()

            binding.apply {
                exhibitDurationBar.max = mediaPlayer!!.duration
                exhibitDurationBar.progress = 0
                exhibitDurationTime.text = helpFunctions.convertToTimerMode(mediaPlayer!!.duration)
                exhibitCurrentTime.text = helpFunctions.convertToTimerMode(mediaPlayer!!.currentPosition)
            }

            listenProgress(binding)

            mediaPlayer!!.setOnPreparedListener { onSuccess() }
        } catch (e: Exception) {
            activity.runOnUiThread { Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show() }
        }
    }

    fun playAudio(activity: MainActivity, binding: FragmentExhibitBinding) {
        scope.launch {
            when (playerStatus) {
                "pause" -> {
                    mediaPlayer!!.pause()
                    playerStatus = "play"

                    activity.runOnUiThread { binding.exhibitPlayIc.setImageResource(R.drawable.ic_play_selector) }
                }
                "play" -> {
                    try {
                        mediaPlayer!!.start()
                        playerStatus = "pause"

                        activity.runOnUiThread { binding.exhibitPlayIc.setImageResource(R.drawable.ic_pause_selector) }
                        initProgressListener(activity, binding)
                    } catch (e: Exception) {
                        Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun pauseAudio(binding: FragmentExhibitBinding) {
        if (playerStatus == "pause") {
            mediaPlayer!!.pause()
            mediaPlayer!!.stop()
            playerStatus = "play"

            binding.exhibitPlayIc.setImageResource(R.drawable.ic_play_selector)
        }
    }

    fun changeAudioSpeed(speed: Float, activity: MainActivity, binding: FragmentExhibitBinding) {
        val playBackParams = PlaybackParams()
        playBackParams.speed = speed
        this.speed = speed
        mediaPlayer?.playbackParams = playBackParams

        if (playerStatus == "play") {
            mediaPlayer!!.pause()
            playerStatus = "play"

            activity.runOnUiThread { binding.exhibitPlayIc.setImageResource(R.drawable.ic_play_selector) }
        }
    }

    private suspend fun initProgressListener(activity: MainActivity, binding: FragmentExhibitBinding) {
        while (playerStatus == "pause") {
            scope.launch {
                activity.runOnUiThread {
                    binding.apply {
                        if (mediaPlayer?.isPlaying == true) {
                            exhibitCurrentTime.text = helpFunctions.convertToTimerMode(mediaPlayer!!.currentPosition)
                            exhibitDurationBar.progress = mediaPlayer!!.currentPosition
                        }
                    }
                }
            }

            delay(10)

            binding.exhibitDurationBar.apply {
                if (progress == max) {
                    playerStatus = "play"

                    mediaPlayer?.seekTo(0)
                    scope.launch {
                        activity.runOnUiThread {
                            binding.apply {
                                exhibitPlayIc.setImageResource(R.drawable.ic_play_selector)
                                exhibitDurationBar.progress = 0
                                exhibitCurrentTime.text = helpFunctions.convertToTimerMode(mediaPlayer!!.currentPosition)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun listenProgress(binding: FragmentExhibitBinding) {
        binding.exhibitDurationBar.setOnSeekBarChangeListener(
            helpFunctions.createSeekBarProgressListener {
                mediaPlayer!!.seekTo(it)
                scope.launch { binding.exhibitCurrentTime.text = helpFunctions.convertToTimerMode(mediaPlayer!!.currentPosition) }
            }
        )
    }
}