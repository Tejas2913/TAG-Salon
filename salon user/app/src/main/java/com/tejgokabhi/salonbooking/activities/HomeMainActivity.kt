package com.tejgokabhi.salonbooking.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.tejgokabhi.salonbooking.R
import com.tejgokabhi.salonbooking.UserLocationManager
import com.tejgokabhi.salonbooking.databinding.ActivityHomeMainBinding
import com.tejgokabhi.salonbooking.fragments.HomeFragment
import java.util.*

class HomeMainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityHomeMainBinding.inflate(layoutInflater) }
    private val LOCATION_REQUEST_CODE = 1001  // Request code for permission

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        requestLocationPermission()
        val navView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_home_main)
        navView.setupWithNavController(navController)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
        } else {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            val settingsClient = LocationServices.getSettingsClient(this)

            settingsClient.checkLocationSettings(builder.build())
                .addOnSuccessListener {
                    getUserArea { area ->
                        Log.d("UserLocation", "Detected area: $area")
                        UserLocationManager.userLocation = area

                        if (area != null) {
                            //Toast.makeText(this, "You're near $area", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    if (exception is ResolvableApiException) {
                        try {
                            exception.startResolutionForResult(this, 1002)
                        } catch (e: Exception) {
                            Log.e("UserLocation", "Error showing GPS prompt: ${e.message}")
                        }
                    }
                }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserArea { area ->
                    Log.d("UserLocation", "Detected area: $area")
                    if (area != null) {
                        Toast.makeText(this, "You're near $area", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Location permission is required!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1002) {
            if (resultCode == RESULT_OK) {
                Log.d("UserLocation", "User enabled GPS")
                getUserArea { area ->
                    Log.d("UserLocation", "Detected area: $area")
                    if (area != null) {
                        Toast.makeText(this, "You're near $area", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.e("UserLocation", "User denied GPS enable request")
                Toast.makeText(this, "Please enable GPS for accurate location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getUserArea(callback: (String?) -> Unit) {
        Log.d("UserLocation", "getUserArea() called")  // ✅ Log before requesting location

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("UserLocation", "Permission not granted")
            callback(null)
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 3000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(1000)
            .setMaxUpdateDelayMillis(3000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location: Location? = locationResult.lastLocation
                Log.d("UserLocation", "Location result received: $location")

                if (location != null) {
                    Log.d("UserLocation", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")

                    val geocoder = Geocoder(this@HomeMainActivity, Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val area = addresses[0].subLocality ?: addresses[0].locality
                            Log.d("UserLocation", "Detected area: $area")
                            UserLocationManager.userLocation = area
                            Log.d("UserLocationManager", "Stored location: ${UserLocationManager.userLocation}")
                            callback(area)
                        } else {
                            Log.e("UserLocation", "Geocoder returned empty address list")
                            callback(null)
                        }
                    } catch (e: Exception) {
                        Log.e("UserLocation", "Geocoder failed: ${e.message}")
                        callback(null)
                    }
                } else {
                    Log.e("UserLocation", "Location is null")
                    callback(null)
                }

                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        Log.d("UserLocation", "Location request started")  // ✅ Log after requesting location
    }
}
