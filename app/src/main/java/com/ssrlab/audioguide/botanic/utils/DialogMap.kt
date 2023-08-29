package com.ssrlab.audioguide.botanic.utils

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.ssrlab.audioguide.botanic.MainActivity
import com.ssrlab.audioguide.botanic.R
import com.ssrlab.audioguide.botanic.databinding.ViewMapBottomSheetBinding
import com.ssrlab.audioguide.botanic.db.ExhibitObject

class DialogMap(
    private val mainActivity: MainActivity,
    private val point: ExhibitObject,
    private val viewAnnotation: View,
    private val viewAnnotationArray: ArrayList<View>,
    private val userCoordinates: Point,
    private val navigation: MapboxNavigation
    ): BottomSheetDialogFragment() {

    private lateinit var binding: ViewMapBottomSheetBinding

    override fun getTheme() = R.style.BottomSheetDialogTheme

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

        binding.bottomSheetRoute.setOnClickListener {

                navigation.requestRoutes(
                    RouteOptions
                        .builder()
                        .applyDefaultNavigationOptions()
                        .profile(DirectionsCriteria.PROFILE_WALKING)
                        .coordinatesList(listOf(userCoordinates, Point.fromLngLat(point.lng, point.lat)))
                        .build(),

                    setUpCallback()
                )
        }

        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        viewAnnotation.findViewById<ConstraintLayout>(R.id.view_map_parent).background = ContextCompat.getDrawable(requireContext(), R.drawable.background_map_point_inactive)
        viewAnnotation.findViewById<TextView>(R.id.view_map_text).setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
    }

    private fun setUpCallback() : NavigationRouterCallback {
        val callback = object : NavigationRouterCallback {
            override fun onCanceled(
                routeOptions: RouteOptions,
                routerOrigin: RouterOrigin
            ) {}

            override fun onFailure(
                reasons: List<RouterFailure>,
                routeOptions: RouteOptions
            ) {
                Toast.makeText(mainActivity, resources.getText(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
            }

            @SuppressLint("MissingPermission")
            override fun onRoutesReady(
                routes: List<NavigationRoute>,
                routerOrigin: RouterOrigin
            ) {
                navigation.setNavigationRoutes(routes)
                navigation.startTripSession()

                dismiss()

                for (i in viewAnnotationArray) {
                    i.findViewById<ConstraintLayout>(R.id.view_map_parent).background = ContextCompat.getDrawable(requireContext(), R.drawable.background_map_point_inactive)
                    i.findViewById<TextView>(R.id.view_map_text).setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }

                viewAnnotation.findViewById<ConstraintLayout>(R.id.view_map_parent).background = ContextCompat.getDrawable(requireContext(), R.drawable.background_map_point_active)
                viewAnnotation.findViewById<TextView>(R.id.view_map_text).setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }

        return callback
    }
}