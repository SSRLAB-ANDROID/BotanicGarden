package com.ssrlab.audioguide.botanic.vm

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.ssrlab.audioguide.botanic.R
import com.ssrlab.audioguide.botanic.databinding.FragmentExhibitBinding
import com.ssrlab.audioguide.botanic.utils.HelpFunctions
import kotlinx.coroutines.*

class PlayerViewModel: ViewModel() {

    private var mpStatus = "play"
    private var viewModelPlayerStatus = 0

    private val mediaJob = Job()
    private val mediaScope = CoroutineScope(Dispatchers.IO + mediaJob)
    private var mediaPlayer: MediaPlayer? = null

    private var binding: FragmentExhibitBinding? = null
    private val helpFunctions = HelpFunctions()

    fun initializeMediaPlayer(uri: Uri, context: Context, binding: FragmentExhibitBinding){

        if (viewModelPlayerStatus == 0) {

            mpStatus = "play"

            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setDataSource(context, uri)
            mediaPlayer!!.prepare()

            this.binding = binding

            binding.apply {
                exhibitDurationBar.max = mediaPlayer!!.duration
                exhibitDurationBar.progress = 0
                exhibitPlayIc.setImageResource(R.drawable.ic_play_selector)
            }

            listenProgress(mediaPlayer!!)

            viewModelPlayerStatus = 1
        }
    }

    fun playAudio(context: Context, binding: FragmentExhibitBinding){
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
                    mediaScope.launch { initProgressListener(mediaPlayer!!, binding) }
                    binding.exhibitPlayIc.setImageResource(R.drawable.ic_pause_selector)
                }
                "play" -> {
                    try {
                        mediaPlayer!!.start()
                        mediaScope.launch { initProgressListener(mediaPlayer!!, binding) }
                        mpStatus = "pause"
                        binding.exhibitPlayIc.setImageResource(R.drawable.ic_pause_selector)

                    } catch (e: Exception){
                        Toast.makeText(context, "Player error: ${e.message.toString()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun mpStop(){
        mpStatus = "stop"

        if (mediaPlayer?.isPlaying == true){
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
        } else mediaPlayer?.release()
    }

    private suspend fun initProgressListener(mediaPlayer: MediaPlayer, binding: FragmentExhibitBinding){
        while (mpStatus == "pause") {
            binding.apply {
                exhibitCurrentTime.text = helpFunctions.convertToTimerMode(mediaPlayer.currentPosition)
                exhibitDurationBar.progress = mediaPlayer.currentPosition
            }
            delay(250)

            binding.exhibitDurationBar.apply {
                if (progress == max) {
                    mpStatus = "play"
                    delay(250)

                    mediaPlayer.seekTo(0)
                    binding.apply {
                        exhibitPlayIc.setImageResource(R.drawable.ic_play_selector)
                        exhibitDurationBar.progress = 0
                        exhibitCurrentTime.text = helpFunctions.convertToTimerMode(mediaPlayer.currentPosition)
                    }
                }
            }
        }
    }

    private fun listenProgress(mediaPlayer: MediaPlayer){
        binding!!.exhibitDurationBar.setOnSeekBarChangeListener(helpFunctions.createSeekBarProgressListener {
            mediaPlayer.seekTo(it)
            binding!!.exhibitCurrentTime.text = helpFunctions.convertToTimerMode(mediaPlayer.currentPosition)
        })
    }

    override fun onCleared() {
        super.onCleared()

        mpStop()
    }
}
