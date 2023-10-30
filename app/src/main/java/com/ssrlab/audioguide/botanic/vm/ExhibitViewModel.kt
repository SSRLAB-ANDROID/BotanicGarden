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
    val id = MutableLiveData<Int>()

    //Observable for forward and backward
    val forwardObserver = MutableLiveData<Boolean>()
    val backwardObserver = MutableLiveData<Boolean>()

    //List of objects
    private var list = arrayListOf<ExhibitObject>()
    fun setList(list: ArrayList<ExhibitObject>) { this.list = list }
    fun getList() = list

    //Volume
    var isVolumeOn = MutableLiveData<Boolean>()

    //Tab Link
    private var tabLink = ""
    fun setTabLink(link: String) { tabLink = link }
    fun getTabLink() = tabLink

    //MapHelper
    private var point = -1
    fun setPoint(point: Int) { this.point = point }
    fun getPoint() = point
}