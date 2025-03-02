package com.ssrlab.audioguide.botanic.utils

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import com.ssrlab.audioguide.botanic.MainActivity
import com.ssrlab.audioguide.botanic.R
import com.ssrlab.audioguide.botanic.databinding.FragmentExhibitBinding
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException

object BotanicMediaPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var currentPosition = 0
    private val helpFunctions = HelpFunctions()

    private var speed: Float? = null
    private var playerStatus = "stopped"

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private lateinit var uri: Uri

    fun initializeMediaPlayer(
        activity: MainActivity,
        binding: FragmentExhibitBinding,
        file: File,
        onSuccess: () -> Unit
    ) {
        mediaPlayer = MediaPlayer()

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
            mediaPlayer?.reset()
            setDataSource(activity, file)

            if (speed != null) {
                val playBackParams = PlaybackParams()
                playBackParams.speed = speed!!
                mediaPlayer!!.playbackParams = playBackParams
            }
            mediaPlayer?.prepare()

            mediaPlayer?.setOnPreparedListener {
                binding.apply {
                    exhibitDurationBar.max = mediaPlayer!!.duration
                    exhibitDurationBar.progress = 0
                    exhibitDurationTime.text =
                        helpFunctions.convertToTimerMode(mediaPlayer!!.duration)
                    exhibitCurrentTime.text =
                        helpFunctions.convertToTimerMode(mediaPlayer!!.currentPosition)
                }

                listenProgress(binding)
                onSuccess()
            }

            mediaPlayer?.setOnCompletionListener {
                playerStatus = "stopped"
                currentPosition = 0
                binding.exhibitPlayIc.setImageResource(R.drawable.ic_play_selector)
                binding.exhibitDurationBar.progress = 0
            }

        } catch (e: IOException) {
            activity.runOnUiThread {
                Toast.makeText(
                    activity,
                    "Ошибка загрузки аудио",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: IllegalStateException) {
            activity.runOnUiThread {
                Toast.makeText(
                    activity,
                    "Ошибка плеера",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun playAudio(activity: MainActivity, binding: FragmentExhibitBinding) {
        scope.launch {
            try {
                when (playerStatus) {
                    "pause" -> {
                        mediaPlayer!!.pause()
                        playerStatus = "play"

                        activity.runOnUiThread { binding.exhibitPlayIc.setImageResource(R.drawable.ic_play_selector) }
                    }

                    "play" -> {
                        mediaPlayer!!.start()
                        playerStatus = "pause"

                        activity.runOnUiThread { binding.exhibitPlayIc.setImageResource(R.drawable.ic_pause_selector) }
                        initProgressListener(activity, binding)
                    }
                }
            } catch (e: IllegalStateException) {
                activity.runOnUiThread {
                    Toast.makeText(
                        activity,
                        "Невозможно воспроизвести аудио",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun pauseAudio(binding: FragmentExhibitBinding) {
        if (playerStatus == "playing") {
            mediaPlayer?.pause()
            currentPosition = mediaPlayer?.currentPosition ?: 0
            playerStatus = "paused"

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

    private suspend fun initProgressListener(
        activity: MainActivity,
        binding: FragmentExhibitBinding
    ) {
        while (playerStatus == "playing") {
            scope.launch {
                activity.runOnUiThread {
                    binding.apply {
                        if (mediaPlayer?.isPlaying == true) {
                            exhibitCurrentTime.text =
                                helpFunctions.convertToTimerMode(mediaPlayer!!.currentPosition)
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
                                exhibitCurrentTime.text =
                                    helpFunctions.convertToTimerMode(mediaPlayer!!.currentPosition)
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
                scope.launch {
                    binding.exhibitCurrentTime.text =
                        helpFunctions.convertToTimerMode(mediaPlayer!!.currentPosition)
                }
            }
        )
    }

    private fun setDataSource(activity: MainActivity, file: File) {
        uri = file.toUri()
        mediaPlayer!!.setDataSource(activity, uri)
    }
}