package com.ssrlab.audioguide.botanic.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import com.ssrlab.audioguide.botanic.MainActivity
import com.ssrlab.audioguide.botanic.R
import com.ssrlab.audioguide.botanic.databinding.FragmentMapBinding
import com.ssrlab.audioguide.botanic.vm.ExhibitViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentMap: Fragment() {

    private lateinit var binding: FragmentMapBinding
    private lateinit var mainActivity: MainActivity
    private var permissionConstant = false
    private var mapView: MapView? = null

    private val viewModel: ExhibitViewModel by activityViewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMapBinding.inflate(layoutInflater)

        mapView = binding.map
        mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS)

        checkPermission()
        setLocationAction()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mainActivity)

        binding.mapBack.setOnClickListener {
            mainActivity.onBackPressedDispatcher.onBackPressed()
        }

        return binding.root
    }

    private fun setLocationAction() {
        scope.launch {
            if (permissionConstant) {

                mainActivity.runOnUiThread{
                    setUpMapBox()
                }

                binding.mapPosition.apply {
                    binding.mapPositionIc.setImageResource(R.drawable.ic_location_enabled)
                    background = ContextCompat.getDrawable(mainActivity, R.drawable.background_green_button)
                    setOnClickListener {

                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                if (location != null) {
                                    val cameraSettings = cameraOptions {
                                        center(Point.fromLngLat(location.longitude, location.latitude))
                                    }
                                    mapView?.camera?.flyTo(cameraSettings)
                                }
                            }
                    }
                }
            } else {
                binding.mapPosition.apply {
                    binding.mapPositionIc.setImageResource(R.drawable.ic_location_disabled)
                    background = ContextCompat.getDrawable(mainActivity, R.drawable.background_disabled_button)
                    setOnClickListener { requestLocationPermission() }
                }
            }

            delay(2000)
            checkPermission()
            setLocationAction()
        }
    }

    private fun setUpMapBox() {
        mapView?.location?.apply {
            enabled = true
            pulsingEnabled = true
            pulsingColor = ContextCompat.getColor(mainActivity, R.color.map_red)
        }

        mapView?.location?.locationPuck = LocationPuck2D(
            topImage = AppCompatResources.getDrawable(mainActivity, R.drawable.ic_location_marker),
            bearingImage = AppCompatResources.getDrawable(mainActivity, R.drawable.ic_location_marker),
            shadowImage = AppCompatResources.getDrawable(mainActivity, R.drawable.ic_location_marker),
            scaleExpression = interpolate {
                linear()
                zoom()
                stop {
                    literal(0.0)
                    literal(0.6)
                }
                stop {
                    literal(20.0)
                    literal(1.0)
                }
            }.toJson()
        )
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            mainActivity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            PackageManager.PERMISSION_GRANTED)
    }

    private fun checkPermission() {
        permissionConstant = ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
}