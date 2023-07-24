package com.ssrlab.audioguide.botanic.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssrlab.audioguide.botanic.R
import com.ssrlab.audioguide.botanic.databinding.FragmentListBinding
import com.ssrlab.audioguide.botanic.db.ExhibitObject
import com.ssrlab.audioguide.botanic.rv.ListAdapter
import com.ssrlab.audioguide.botanic.vm.ExhibitViewModel

class FragmentList: Fragment() {

    private lateinit var binding: FragmentListBinding
    private lateinit var adapter: ListAdapter

    private lateinit var navController: NavController
    private lateinit var actionFromHeader: () -> Unit
    private lateinit var actionFromItem: () -> Unit

    private val viewModel: ExhibitViewModel by activityViewModels()

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
        actionFromHeader = { navController.navigate(R.id.action_fragmentList_to_fragmentInfo) }
        actionFromItem = { navController.navigate(R.id.action_fragmentList_to_fragmentExhibit) }
    }

    override fun onResume() {
        super.onResume()

        val array = arrayListOf(ExhibitObject(), ExhibitObject(), ExhibitObject(), ExhibitObject())

        adapter = ListAdapter(array, requireContext(), viewModel, actionFromHeader, actionFromItem)
        binding.rvList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FragmentList.adapter
        }
    }
}