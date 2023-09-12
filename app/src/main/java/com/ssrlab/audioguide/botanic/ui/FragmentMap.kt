package com.ssrlab.audioguide.botanic.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.LocationComponentConstants
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineScaleValue
import com.ssrlab.audioguide.botanic.MainActivity
import com.ssrlab.audioguide.botanic.R
import com.ssrlab.audioguide.botanic.databinding.FragmentMapBinding
import com.ssrlab.audioguide.botanic.databinding.ViewMapBinding
import com.ssrlab.audioguide.botanic.db.ExhibitObject
import com.ssrlab.audioguide.botanic.utils.DialogMap
import com.ssrlab.audioguide.botanic.vm.ExhibitViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentMap: Fragment() {

    private lateinit var binding: FragmentMapBinding
    private lateinit var mainActivity: MainActivity

    private val annotationArray = arrayListOf<View>()

    private var permissionConstant = false
    private val pointActivatedArray = arrayListOf<Boolean>()
    private var mapView: MapView? = null
    private lateinit var mapboxMap: MapboxMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewAnnotationManager: ViewAnnotationManager

    private lateinit var routeLineResources: RouteLineResources
    private lateinit var options: MapboxRouteLineOptions
    private lateinit var routeLineApi: MapboxRouteLineApi
    private lateinit var routesObserver: RoutesObserver

    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {
            @SuppressLint("MissingPermission")
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerRoutesObserver(routesObserver)
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
            }
        }
    )

    private val exhibitViewModel: ExhibitViewModel by activityViewModels()
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
        viewAnnotationManager = binding.map.viewAnnotationManager
        mapboxMap = binding.map.getMapboxMap()
        mapView?.scalebar?.enabled = false
        mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS).apply {
            for (i in exhibitViewModel.getList()) {
                val point = Point.fromLngLat(i.lng, i.lat)
                val pointNumber = i.placeId.toString()
                addPoint(point, i, pointNumber)
                pointActivatedArray.add(false)
            }
        }

        routeLineResources = RouteLineResources.Builder()
            .routeLineColorResources(RouteLineColorResources.Builder()
                .routeDefaultColor(ContextCompat.getColor(mainActivity, R.color.map_red))
                .routeCasingColor(ContextCompat.getColor(mainActivity, R.color.map_red))
                .routeClosureColor(ContextCompat.getColor(mainActivity, R.color.map_red))
                .routeHeavyCongestionColor(ContextCompat.getColor(mainActivity, R.color.map_red))
                .routeLowCongestionColor(ContextCompat.getColor(mainActivity, R.color.map_red))
                .routeModerateCongestionColor(ContextCompat.getColor(mainActivity, R.color.map_red))
                .routeSevereCongestionColor(ContextCompat.getColor(mainActivity, R.color.map_red))
                .routeUnknownCongestionColor(ContextCompat.getColor(mainActivity, R.color.map_red))
                .routeDefaultColor(ContextCompat.getColor(mainActivity, R.color.map_red))
                .alternativeRouteCasingColor(ContextCompat.getColor(mainActivity, R.color.map_red))
                .alternativeRouteClosureColor(ContextCompat.getColor(mainActivity, R.color.map_red))
                .alternativeRouteHeavyCongestionColor(ContextCompat.getColor(mainActivity, R.color.map_red))
                .alternativeRouteLowCongestionColor(ContextCompat.getColor(mainActivity, R.color.map_red))
                .alternativeRouteModerateCongestionColor(ContextCompat.getColor(mainActivity, R.color.map_red))
                .alternativeRouteRestrictedRoadColor(ContextCompat.getColor(mainActivity, R.color.map_red))
                .alternativeRouteSevereCongestionColor(ContextCompat.getColor(mainActivity, R.color.map_red))
                .alternativeRouteUnknownCongestionColor(ContextCompat.getColor(mainActivity, R.color.map_red))
                .alternativeRouteDefaultColor(ContextCompat.getColor(mainActivity, R.color.map_red))
                .build())
            .routeLineScaleExpression(buildScaleExpression(
                listOf(
                    RouteLineScaleValue(3f, 2f, 1f),
                    RouteLineScaleValue(5f, 3f, 1f),
                    RouteLineScaleValue(6f, 4f, 1f),
                    RouteLineScaleValue(7f, 5f, 1f),
                    RouteLineScaleValue(8f, 6f, 1f),
                    RouteLineScaleValue(9f, 7f, 1f)
                )
            ))
            .build()

        options = MapboxRouteLineOptions.Builder(mainActivity)
            .withVanishingRouteLineEnabled(true)
            .withRouteLineResources(routeLineResources)
            .withRouteLineBelowLayerId(LocationComponentConstants.LOCATION_INDICATOR_LAYER)
            .build()

        routeLineApi = MapboxRouteLineApi(options)

        routesObserver = RoutesObserver {
            routeLineApi.setNavigationRoutes(it.navigationRoutes) { value ->
                mapboxMap.getStyle()?.apply {
                    MapboxRouteLineView(options).renderRouteDrawData(this, value)
                }
            }
        }

        checkPermission()
        setLocationAction()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mainActivity)

        binding.mapBack.setOnClickListener {
            mainActivity.getNavController().popBackStack()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        if (exhibitViewModel.getPoint() != -1) {

            val exhibitObject = exhibitViewModel.getExhibitObject()
            val point = Point.fromLngLat(exhibitObject.lng, exhibitObject.lat)

            val cameraSettings = cameraOptions {
                center(point)
            }
            mapView?.camera?.flyTo(cameraSettings)
        }
    }

    override fun onStop() {
        super.onStop()

        exhibitViewModel.setPoint(-1)
    }

    override fun onDestroy() {
        super.onDestroy()

        mapboxNavigation.stopTripSession()
        mapboxNavigation.setNavigationRoutes(listOf())
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

    @SuppressLint("MissingPermission")
    private fun addPoint(point: Point, pointObject: ExhibitObject, pointNumber: String) {
        val viewAnnotation = viewAnnotationManager.addViewAnnotation(
            resId = R.layout.view_map,
            options = viewAnnotationOptions {
                geometry(point)
            }
        )
        annotationArray.add(viewAnnotation)

        viewAnnotation.findViewById<TextView>(R.id.view_map_text).text = pointNumber
        viewAnnotation.setOnClickListener {
            val cameraSettings = cameraOptions {
                center(point)
            }
            mapView?.camera?.flyTo(cameraSettings)

            var currentPoint = Point.fromLngLat(0.0, 0.0)

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) currentPoint = Point.fromLngLat(location.longitude, location.latitude)

                DialogMap(mainActivity, pointObject, viewAnnotation, annotationArray, currentPoint, mapboxNavigation).show(parentFragmentManager, pointObject.placeName)

                viewAnnotation.findViewById<ConstraintLayout>(R.id.view_map_parent).background = ContextCompat.getDrawable(requireContext(), R.drawable.background_map_point_active)
                viewAnnotation.findViewById<TextView>(R.id.view_map_text).setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }


        }
        ViewMapBinding.bind(viewAnnotation)
    }

    private fun buildScaleExpression(scalingValues: List<RouteLineScaleValue>) : Expression {
        val expressionBuilder = Expression.ExpressionBuilder("interpolate")
        expressionBuilder.addArgument(Expression.exponential { literal(1.5) })
        expressionBuilder.zoom()
        scalingValues.forEach { routeLineScaleValue ->
            expressionBuilder.stop {
                this.literal(routeLineScaleValue.scaleStop.toDouble())
                product {
                    literal(routeLineScaleValue.scaleMultiplier.toDouble())
                    literal(routeLineScaleValue.scale.toDouble())
                }
            }
        }

        return expressionBuilder.build()
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