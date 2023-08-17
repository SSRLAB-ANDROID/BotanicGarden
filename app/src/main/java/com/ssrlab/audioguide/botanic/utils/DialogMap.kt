package com.ssrlab.audioguide.botanic.utils

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ssrlab.audioguide.botanic.R
import com.ssrlab.audioguide.botanic.databinding.ViewMapBottomSheetBinding
import com.ssrlab.audioguide.botanic.db.ExhibitObject

class DialogMap(
    private val point: ExhibitObject,
    private val viewAnnotation: View
    ): BottomSheetDialogFragment() {

    private lateinit var binding: ViewMapBottomSheetBinding

    override fun getTheme() = R.style.AppBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = ViewMapBottomSheetBinding.inflate(layoutInflater)

        binding.apply {
            bottomSheetImage.load(point.imagePreview) {
                crossfade(true)
                    transformations(RoundedCornersTransformation(32f))
            }
            bottomSheetTitle.text = point.placeName
        }

        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        viewAnnotation.findViewById<ConstraintLayout>(R.id.view_map_parent).background = ContextCompat.getDrawable(requireContext(), R.drawable.background_map_point_inactive)
        viewAnnotation.findViewById<TextView>(R.id.view_map_text).setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
    }
}