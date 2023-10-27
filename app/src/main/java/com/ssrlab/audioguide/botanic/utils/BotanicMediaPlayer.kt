package com.ssrlab.audioguide.botanic.utils

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.widget.Toast
import com.ssrlab.audioguide.botanic.MainActivity
import com.ssrlab.audioguide.botanic.R
import com.ssrlab.audioguide.botanic.databinding.FragmentExhibitBinding
import kotlinx.coroutines.*
import java.io.IOException

object BotanicMediaPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private val helpFunctions = HelpFunctions()

    private var speed: Float? = null
    private var playerStatus = ""

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Unconfined + job)

    fun initializeMediaPlayer(activity: MainActivity, binding: FragmentExhibitBinding, uri: Uri) {
        playerStatus = "play"
        mediaPlayer = MediaPlayer()

        try {
            pauseAudio(binding)
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
        } catch (e:IOException) {
            activity.runOnUiThread { Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show() }
        }
    }

    fun playAudio(activity: MainActivity, binding: FragmentExhibitBinding) {
        scope.launch {
            when (playerStatus) {
                "pause" -> {
                    mediaPlayer!!.pause()
                    playerStatus = "play"

                    binding.exhibitPlayIc.setImageResource(R.drawable.ic_play_selector)
                }
                "play" -> {
                    try {
                        mediaPlayer!!.start()
                        playerStatus = "pause"

                        binding.exhibitPlayIc.setImageResource(R.drawable.ic_pause_selector)
                        initProgressListener(binding)
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
            playerStatus = "play"

            binding.exhibitPlayIc.setImageResource(R.drawable.ic_play_selector)
        }
    }

    fun changeAudioSpeed(speed: Float) {
        val playBackParams = PlaybackParams()
        playBackParams.speed = speed
        this.speed = speed
        mediaPlayer?.playbackParams = playBackParams
    }

    private suspend fun initProgressListener(binding: FragmentExhibitBinding) {
        while (playerStatus == "pause") {
            scope.launch {
                binding.apply {
                    if (mediaPlayer?.isPlaying == true) {
                        exhibitCurrentTime.text = helpFunctions.convertToTimerMode(mediaPlayer!!.currentPosition)
                        exhibitDurationBar.progress = mediaPlayer!!.currentPosition
                    }
                }
            }

            delay(10)

            binding.exhibitDurationBar.apply {
                if (progress == max) {
                    playerStatus = "play"

                    mediaPlayer?.seekTo(0)
                    scope.launch {
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

    private fun listenProgress(binding: FragmentExhibitBinding) {
        binding.exhibitDurationBar.setOnSeekBarChangeListener(
            helpFunctions.createSeekBarProgressListener {
                mediaPlayer!!.seekTo(it)
                scope.launch { binding.exhibitCurrentTime.text = helpFunctions.convertToTimerMode(mediaPlayer!!.currentPosition) }
            }
        )
    }
}