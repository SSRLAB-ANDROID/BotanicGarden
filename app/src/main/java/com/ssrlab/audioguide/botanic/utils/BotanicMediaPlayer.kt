package com.ssrlab.audioguide.botanic.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import com.ssrlab.audioguide.botanic.MainActivity
import com.ssrlab.audioguide.botanic.R
import com.ssrlab.audioguide.botanic.databinding.FragmentExhibitBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
        context: Context,
        file: File,
        onSuccess: () -> Unit
    ) {
        releaseMediaPlayer(file)
        mediaPlayer = MediaPlayer()

        try {
            mediaPlayer?.reset()
            setDataSource(activity, file)

            mediaPlayer?.prepareAsync()

            mediaPlayer?.setOnPreparedListener {
                if (speed != null) {
                    try {
                        mediaPlayer?.playbackParams =
                            PlaybackParams().apply { this.speed = speed }
                    } catch (e: Exception) {
                        activity.runOnUiThread {
                            Toast.makeText(
                                activity,
                                context.getString(R.string.player_error),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                binding.exhibitDurationBar.max = mediaPlayer!!.duration
                binding.exhibitDurationBar.progress = 0
                binding.exhibitDurationTime.text =
                    helpFunctions.convertToTimerMode(mediaPlayer!!.duration)
                binding.exhibitCurrentTime.text = helpFunctions.convertToTimerMode(0)

                mediaPlayer!!.seekTo(currentPosition)
                playerStatus = "ready"

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
                    context.getString(R.string.audio_loading_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: IllegalStateException) {
            activity.runOnUiThread {
                Toast.makeText(
                    activity,
                    context.getString(R.string.player_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun playAudio(activity: MainActivity, binding: FragmentExhibitBinding, context: Context) {
        scope.launch {
            try {
                when (playerStatus) {
                    "paused", "ready" -> {
                        mediaPlayer?.start()
                        playerStatus = "playing"

                        activity.runOnUiThread { binding.exhibitPlayIc.setImageResource(R.drawable.ic_pause_selector) }
                        initProgressListener(activity, binding)
                    }

                    "playing" -> {
                        mediaPlayer?.pause()
                        currentPosition = mediaPlayer?.currentPosition ?: 0
                        playerStatus = "paused"

                        activity.runOnUiThread { binding.exhibitPlayIc.setImageResource(R.drawable.ic_play_selector) }
                    }
                }
            } catch (e: IllegalStateException) {
                activity.runOnUiThread {
                    Toast.makeText(
                        activity,
                        context.getString(R.string.impossible_to_extract_audio),
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

    fun changeAudioSpeed(speed: Float) {
        val playBackParams = PlaybackParams()
        playBackParams.speed = speed
        this.speed = speed
        mediaPlayer?.playbackParams = playBackParams
    }

    private suspend fun initProgressListener(
        activity: MainActivity,
        binding: FragmentExhibitBinding
    ) {
        while (playerStatus == "playing") {
            scope.launch {
                activity.runOnUiThread {
                    mediaPlayer?.let {
                        if (it.isPlaying) {
                            binding.exhibitCurrentTime.text =
                                helpFunctions.convertToTimerMode(it.currentPosition)
                            binding.exhibitDurationBar.progress = it.currentPosition
                        }
                    }
                }
            }
            delay(500)
        }
    }

    private fun listenProgress(binding: FragmentExhibitBinding) {
        binding.exhibitDurationBar.setOnSeekBarChangeListener(
            helpFunctions.createSeekBarProgressListener {
                mediaPlayer?.seekTo(it)
                currentPosition = it
                scope.launch {
                    binding.exhibitCurrentTime.text = helpFunctions.convertToTimerMode(it)
                }
            }
        )
    }

    private fun setDataSource(activity: MainActivity, file: File) {
        uri = file.toUri()
        mediaPlayer!!.setDataSource(activity, uri)
    }

    private fun releaseMediaPlayer(file: File) {
        if (::uri.isInitialized) {
            if (uri != file.toUri()) currentPosition = 0
        }
        mediaPlayer?.release()
        mediaPlayer = null
        playerStatus = "stopped"
    }

    fun setVolume(leftVolume: Float, rightVolume: Float) {
        mediaPlayer?.setVolume(leftVolume, rightVolume)
    }
}