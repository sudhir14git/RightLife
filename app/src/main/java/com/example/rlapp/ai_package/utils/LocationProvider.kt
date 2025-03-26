package com.example.rlapp.ai_package.utils

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationProvider(
    private val context: Context,
    private val locationListener: LocationListener
) {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: com.google.android.gms.location.LocationRequest
    private lateinit var locationCallback: LocationCallback

    init {
        initLocationClient()
    }

    private fun initLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    fun createLocationRequest() {
        locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            priority = Priority.PRIORITY_HIGH_ACCURACY
            fastestInterval = FASTEST_INTERVAL
            interval = INTERVAL
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { tourListData ->
                    locationListener.onLocationUpdate(tourListData)
                }
            }
        }
    }

    /*fun createLocationRequest1() = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, TimeUnit.MINUTES.toMillis(5)
    ).apply {
        setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
        setDurationMillis(TimeUnit.MINUTES.toMillis(5))
        setWaitForAccurateLocation(true)
        setMaxUpdates(1)
    }.build()*/

    fun startLocationUpdates() {
        if (checkLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun checkLocationPermission(): Boolean {
        return (context.checkSelfPermission(
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    interface LocationListener {
        fun onLocationUpdate(tourListData: Location)
    }

    companion object {
        private const val FASTEST_INTERVAL: Long = 2000
        private const val INTERVAL: Long = 4000
    }
}