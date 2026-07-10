package com.example.aplikasi_asrama

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import com.example.aplikasi_asrama.databinding.ActivityMapsBinding
import com.example.aplikasi_asrama.location.LocationTracker
import com.example.aplikasi_asrama.viewmodel.LocationViewModel
import com.example.aplikasi_asrama.viewmodel.UserLocation  // ← import yang benar
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var map: GoogleMap
    private lateinit var viewModel: LocationViewModel
    private val locationTracker = LocationTracker(this)
    private val markerMap = mutableMapOf<String, Marker>()
    private var isMapReady = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            startSharing()
        } else {
            Toast.makeText(this, "Izin lokasi diperlukan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[LocationViewModel::class.java]
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        checkPermissions()
    }

    private fun checkPermissions() {
        if (locationTracker.hasLocationPermission()) {
            startSharing()
        } else {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    private fun startSharing() {
        LocationForegroundService.start(this)

        if (isMapReady) {
            lifecycleScope.launch {
                val loc = locationTracker.getCurrentLocation()
                loc?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    map.addMarker(MarkerOptions().position(latLng).title("Saya"))
                }
            }
        }

        lifecycleScope.launch {
            viewModel.otherUsersLocations.collect { users ->
                if (isMapReady) {
                    updateMarkers(users)
                }
            }
        }
    }

    private fun updateMarkers(users: List<UserLocation>) {
        val currentIds = markerMap.keys.toSet()
        val newIds = users.map { it.userId }.toSet()
        for (id in currentIds - newIds) {
            markerMap[id]?.remove()
            markerMap.remove(id)
        }
        for (user in users) {
            val pos = LatLng(user.lat, user.lng)
            val marker = markerMap[user.userId] ?: map.addMarker(
                MarkerOptions().position(pos).title(user.userId)
            )
            marker?.position = pos
            marker?.title = "User: ${user.userId.take(6)}"
            markerMap[user.userId] = marker!!
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        if (locationTracker.hasLocationPermission()) {
            try {
                map.isMyLocationEnabled = true
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
        isMapReady = true

        if (locationTracker.hasLocationPermission()) {
            lifecycleScope.launch {
                val loc = locationTracker.getCurrentLocation()
                loc?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    map.addMarker(MarkerOptions().position(latLng).title("Saya"))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocationForegroundService.stop(this)
        viewModel.removeMyLocation()
    }
}