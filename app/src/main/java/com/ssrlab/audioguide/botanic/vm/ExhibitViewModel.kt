package com.ssrlab.audioguide.botanic.vm

import android.widget.PopupWindow
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.ssrlab.audioguide.botanic.R
import com.ssrlab.audioguide.botanic.databinding.FragmentExhibitBinding
import com.ssrlab.audioguide.botanic.db.ExhibitObject
import com.ssrlab.audioguide.botanic.ui.FragmentExhibit
import com.ssrlab.audioguide.botanic.utils.BotanicMediaPlayer
import com.ssrlab.audioguide.botanic.utils.SWITCH_EXHIBIT_TIMER
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExhibitViewModel : ViewModel() {

    //ExhibitHelper
    private val forwardObserver = MutableLiveData<Boolean>()
    private val backwardObserver = MutableLiveData<Boolean>()

    fun setUpMoveActions(binding: FragmentExhibitBinding, fragment: FragmentExhibit) {
        id.observe(fragment) {
            when (it) {
                0 -> {
                    backwardObserver.value = false
                    forwardObserver.value = true
                }

                list.size - 1 -> {
                    forwardObserver.value = false
                    backwardObserver.value = true
                }

                else -> {
                    forwardObserver.value = true
                    backwardObserver.value = true
                }
            }
        }

        forwardObserver.observe(fragment) {
            if (it) moveButtonsHelper(0, true, binding)
            else moveButtonsHelper(0, false, binding)
        }

        backwardObserver.observe(fragment) {
            if (it) moveButtonsHelper(1, true, binding)
            else moveButtonsHelper(1, false, binding)
        }
    }

    private fun moveButtonsHelper(pos: Int, active: Boolean, binding: FragmentExhibitBinding) {
        binding.apply {
            if (pos == 0) {
                exhibitNextIc.isClickable = active
                exhibitNextIc.isFocusable = active
                if (active) exhibitNextIc.setImageResource(R.drawable.ic_next_selector)
                else exhibitNextIc.setImageResource(R.drawable.ic_next_disabled)
            } else {
                exhibitPreviousIc.isClickable = active
                exhibitPreviousIc.isFocusable = active
                if (active) exhibitPreviousIc.setImageResource(R.drawable.ic_previous_selector)
                else exhibitPreviousIc.setImageResource(R.drawable.ic_previous_disabled)
            }
        }
    }

    fun setUpOrderAction(
        binding: FragmentExhibitBinding,
        scope: CoroutineScope,
        onSuccess: () -> Unit
    ) {
        binding.apply {
            exhibitPreviousIc.setOnClickListener {
                if (id.value != 0) {
                    scope.launch {
                        delay(SWITCH_EXHIBIT_TIMER)

                        id.value = id.value?.minus(1)
                        BotanicMediaPlayer.pauseAudio(this@apply)
                        onSuccess()
                    }
                }
            }

            exhibitNextIc.setOnClickListener {
                if (id.value != list.size - 1) {
                    scope.launch {
                        delay(SWITCH_EXHIBIT_TIMER)

                        id.value = id.value?.plus(1)
                        BotanicMediaPlayer.pauseAudio(this@apply)
                        onSuccess()
                    }
                }
            }
        }
    }

    fun updateExhibit(
        window: PopupWindow,
        binding: FragmentExhibitBinding,
        tabSetup: (ArrayList<String>) -> Unit
    ) {
        exhibitObject = list[id.value!!]

        if (window.isShowing) window.dismiss()

        val imagesArray = arrayListOf<String>()
        for (i in exhibitObject.images.keys) exhibitObject.images[i]?.let { imagesArray.add(it) }
        tabSetup(imagesArray)

        binding.apply {
            TabLayoutMediator(binding.exhibitTabLayout, binding.exhibitPager) { _, _ -> }.attach()

            exhibitInfoText.text = exhibitObject.audioText
            exhibitTitle.text = exhibitObject.placeName.substringAfter(". ")
        }
    }

    /********************************************************************************************************************************************/
    /********************************************************************************************************************************************/
    /********************************************************************************************************************************************/

    //Current object
    private var exhibitObject = ExhibitObject()
    fun getExhibitObject() = exhibitObject

    //Current object ID
    val id = MutableLiveData<Int>()

    //List of objects
    private var list = arrayListOf<ExhibitObject>()
    fun setList(list: ArrayList<ExhibitObject>) {
        this.list = list
    }

    fun getList() = list

    //Tab Link
    private var tabLink = ""
    fun setTabLink(link: String) {
        tabLink = link
    }

    fun getTabLink() = tabLink

    //MapHelper
    private var point = -1
    fun setPoint(point: Int) {
        this.point = point
    }

    fun getPoint() = point

    //Player volume
    private val _isVolumeOn = MutableLiveData(true)
    val isVolumeOn: LiveData<Boolean> get() = _isVolumeOn
}