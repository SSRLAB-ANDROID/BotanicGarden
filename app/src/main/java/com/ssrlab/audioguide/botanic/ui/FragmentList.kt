package com.ssrlab.audioguide.botanic.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssrlab.audioguide.botanic.MainActivity
import com.ssrlab.audioguide.botanic.R
import com.ssrlab.audioguide.botanic.databinding.FragmentListBinding
import com.ssrlab.audioguide.botanic.db.ExhibitObject
import com.ssrlab.audioguide.botanic.rv.ListAdapter
import com.ssrlab.audioguide.botanic.vm.ExhibitViewModel
import kotlinx.coroutines.launch

class FragmentList: Fragment() {

    private lateinit var mainActivity: MainActivity

    private lateinit var binding: FragmentListBinding
    private lateinit var adapter: ListAdapter
    private lateinit var window: PopupWindow

    private lateinit var navController: NavController
    private lateinit var actionFromItem: () -> Unit
    private var list = arrayListOf<ExhibitObject>()

    private val viewModel: ExhibitViewModel by activityViewModels()

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

        binding = FragmentListBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = view.findNavController()
        actionFromItem = { navController.navigate(R.id.action_fragmentList_to_fragmentExhibit) }
    }

    override fun onResume() {
        super.onResume()

        mainActivity.apply {
            getScope().launch {
                list = getDao().getAllExhibits() as ArrayList<ExhibitObject>
                val listByLanguage = arrayListOf<ExhibitObject>()

                var index = 1
                for (i in list) if (i.language == mainActivity.getApp().getLocaleInt()) {
                    val newObject = ExhibitObject(
                        primaryId = i.primaryId,
                        placeId = i.placeId,
                        imagePreview = i.imagePreview,
                        qr = i.qr,
                        placeName = "${i.placeId}. ${i.placeName}",
                        audioText = i.audioText,
                        audio = i.audio,
                        language = i.language,
                        lat = i.lat,
                        lng = i.lng,
                        images = i.images
                    )
                    listByLanguage.add(newObject)
                    index++
                }
                viewModel.setList(listByLanguage)

                runOnUiThread {
                    adapter = ListAdapter(listByLanguage, viewModel, actionFromItem, mainActivity, this@FragmentList.window)
                    binding.rvList.apply {
                        layoutManager = LinearLayoutManager(requireContext())
                        adapter = this@FragmentList.adapter
                    }
                }
            }
        }
    }
}