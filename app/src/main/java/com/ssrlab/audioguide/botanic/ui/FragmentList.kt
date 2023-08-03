package com.ssrlab.audioguide.botanic.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    private lateinit var navController: NavController
    private lateinit var actionFromHeader: () -> Unit
    private lateinit var actionFromItem: () -> Unit
    private var list = arrayListOf<ExhibitObject>()

    private val viewModel: ExhibitViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivity = activity as MainActivity
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
        actionFromHeader = { Toast.makeText(mainActivity, "Language", Toast.LENGTH_SHORT).show() }
        actionFromItem = { navController.navigate(R.id.action_fragmentList_to_fragmentExhibit) }
    }

    override fun onResume() {
        super.onResume()

        mainActivity.apply {
            getScope().launch {
                list = getDao().getAllExhibits() as ArrayList<ExhibitObject>
                viewModel.setList(list)

                runOnUiThread {
                    adapter = ListAdapter(list, viewModel, actionFromHeader, actionFromItem)
                    binding.rvList.apply {
                        layoutManager = LinearLayoutManager(requireContext())
                        adapter = this@FragmentList.adapter
                    }
                }
            }
        }
    }
}