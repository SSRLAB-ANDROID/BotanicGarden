package com.ssrlab.audioguide.botanic.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.ssrlab.audioguide.botanic.MainActivity
import com.ssrlab.audioguide.botanic.R
import com.ssrlab.audioguide.botanic.databinding.FragmentMapBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentMap: Fragment() {

    private lateinit var binding: FragmentMapBinding
    private lateinit var mainActivity: MainActivity
    private var permissionConstant = false
    private var mapView: MapView? = null

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
        setUpActions()

        return binding.root
    }

    private fun setUpActions() {
        scope.launch {
            if (permissionConstant) {
                binding.mapPosition.apply {
                    binding.mapPositionIc.setImageResource(R.drawable.ic_location_enabled)
                    background = ContextCompat.getDrawable(mainActivity, R.drawable.background_green_button)
                    setOnClickListener {
                        Toast.makeText(mainActivity, "Permission granted", Toast.LENGTH_SHORT).show()
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
            setUpActions()
        }
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