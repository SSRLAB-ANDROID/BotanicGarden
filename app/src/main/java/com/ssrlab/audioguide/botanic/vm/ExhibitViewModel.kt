package com.ssrlab.audioguide.botanic.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ssrlab.audioguide.botanic.db.ExhibitObject

class ExhibitViewModel: ViewModel() {

    //Current object
    private var exhibitObject = ExhibitObject()
    fun setChosenItem(exhibitObject: ExhibitObject) { this.exhibitObject = exhibitObject }
    fun getExhibitObject() = exhibitObject

    //Current object ID
    var id = MutableLiveData<Int>()

    //List of objects
    private var list = arrayListOf<ExhibitObject>()
    fun setList(list: ArrayList<ExhibitObject>) { this.list = list }
    fun getList() = list

    //Volume
    private var isVolumeOn = true
    fun getVolumeValue() = isVolumeOn
    fun setVolumeValue(value: Boolean) {
        isVolumeOn = value
    }
}