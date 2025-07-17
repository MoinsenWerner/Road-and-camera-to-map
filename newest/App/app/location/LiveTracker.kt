package com.example.preciseroadmap.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LiveTracker(context: Context) {

    private val fusedLocation = LocationServices.getFusedLocationProviderClient(context)
    private val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()

    private val _points = MutableStateFlow<List<List<Double>>>(emptyList())
    val points: StateFlow<List<List<Double>>> = _points.asStateFlow()

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location: Location = result.lastLocation ?: return
            val lat = location.latitude
            val lon = location.longitude
            val updated = _points.value.toMutableList()
            updated.add(listOf(lat, lon))
            _points.value = updated
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        fusedLocation.requestLocationUpdates(request, callback, Looper.getMainLooper())
    }

    fun stop() {
        fusedLocation.removeLocationUpdates(callback)
    }

    fun reset() {
        _points.value = emptyList()
    }
}