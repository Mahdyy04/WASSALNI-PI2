package com.carpooling.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.carpooling.app.databinding.ActivityLocationPickerBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.IOException
import java.util.Locale

/**
 * Activity for picking a location on the map using OSMDroid (OpenStreetMap).
 * This is a free, open-source alternative to Google Maps - similar to Leaflet.js for web.
 * No API key required!
 */
class LocationPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationPickerBinding
    private lateinit var mapView: MapView
    private var centerMarker: Marker? = null
    private var selectedGeoPoint: GeoPoint? = null
    private var selectedAddress: String = ""
    private var selectedCityName: String = ""
    private var selectedPostalCode: String = ""
    private var locationManager: LocationManager? = null
    private var isRequestingLocation = false

    companion object {
        const val EXTRA_LOCATION_TYPE = "location_type"
        const val RESULT_LATITUDE = "latitude"
        const val RESULT_LONGITUDE = "longitude"
        const val RESULT_ADDRESS = "address"
        const val RESULT_CITY_NAME = "city_name"
        const val RESULT_POSTAL_CODE = "postal_code"
        private const val LOCATION_PERMISSION_REQUEST = 1001
        private const val MAP_ANIMATION_DELAY_MS = 1100L
        
        // Default location: Bizerte, Tunisia (updated for user's location)
        private val DEFAULT_LOCATION = GeoPoint(37.2744, 9.8739)
        private const val DEFAULT_ZOOM = 12.0
        private const val SELECTED_ZOOM = 15.0
    }
    
    // Location listener for getting fresh GPS coordinates
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (isRequestingLocation) {
                isRequestingLocation = false
                val currentGeoPoint = GeoPoint(location.latitude, location.longitude)
                runOnUiThread {
                    mapView.controller.animateTo(currentGeoPoint, SELECTED_ZOOM, 1000L)
                    mapView.postDelayed({
                        updateMarkerAndAddress()
                    }, MAP_ANIMATION_DELAY_MS)
                    Toast.makeText(
                        this@LocationPickerActivity,
                        "Location: ${location.latitude}, ${location.longitude}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                // Stop listening after getting location
                locationManager?.removeUpdates(this)
            }
        }
        
        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure OSMDroid before inflating layout
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName
        
        binding = ActivityLocationPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Set title based on location type
        val locationType = intent.getStringExtra(EXTRA_LOCATION_TYPE) ?: "location"
        title = if (locationType == "departure") {
            getString(R.string.select_departure_location)
        } else {
            getString(R.string.select_destination_location)
        }

        setupMap()
        setupListeners()
    }

    private fun setupMap() {
        mapView = binding.mapView
        
        // Configure the map
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(DEFAULT_ZOOM)
        mapView.controller.setCenter(DEFAULT_LOCATION)
        
        // Create center marker
        centerMarker = Marker(mapView).apply {
            position = DEFAULT_LOCATION
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = ContextCompat.getDrawable(this@LocationPickerActivity, android.R.drawable.ic_menu_mylocation)
            title = getString(R.string.selected_location)
        }
        mapView.overlays.add(centerMarker)
        
        // Listen for map movements - update marker position when user scrolls the map
        mapView.addOnFirstLayoutListener { _, _, _, _, _ ->
            updateMarkerAndAddress()
        }
        
        // Update marker position when map is scrolled
        mapView.setOnTouchListener { _, _ ->
            mapView.postDelayed({
                updateMarkerAndAddress()
            }, 100)
            false
        }
        
        // Try to get current location automatically
        checkLocationPermissionAndGetLocation()
    }

    private fun updateMarkerAndAddress() {
        val center = mapView.mapCenter as GeoPoint
        selectedGeoPoint = center
        
        // Update marker position
        centerMarker?.position = center
        mapView.invalidate()
        
        // Update address
        updateAddressFromLocation(center)
    }

    private fun setupListeners() {
        binding.btnMyLocation.setOnClickListener {
            getCurrentLocation()
        }

        binding.btnConfirm.setOnClickListener {
            if (selectedGeoPoint != null) {
                val intent = Intent().apply {
                    putExtra(RESULT_LATITUDE, selectedGeoPoint!!.latitude)
                    putExtra(RESULT_LONGITUDE, selectedGeoPoint!!.longitude)
                    putExtra(RESULT_ADDRESS, selectedAddress)
                    putExtra(RESULT_CITY_NAME, selectedCityName)
                    putExtra(RESULT_POSTAL_CODE, selectedPostalCode)
                }
                setResult(RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(this, getString(R.string.please_select_location), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun checkLocationPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        try {
            isRequestingLocation = true
            
            // First try to get a fresh location from GPS
            val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
            val isNetworkEnabled = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ?: false
            
            when {
                isGpsEnabled -> {
                    Toast.makeText(this, "Getting GPS location...", Toast.LENGTH_SHORT).show()
                    locationManager?.requestSingleUpdate(
                        LocationManager.GPS_PROVIDER,
                        locationListener,
                        Looper.getMainLooper()
                    )
                }
                isNetworkEnabled -> {
                    Toast.makeText(this, "Getting network location...", Toast.LENGTH_SHORT).show()
                    locationManager?.requestSingleUpdate(
                        LocationManager.NETWORK_PROVIDER,
                        locationListener,
                        Looper.getMainLooper()
                    )
                }
                else -> {
                    isRequestingLocation = false
                    // Fall back to last known location
                    val lastLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    
                    if (lastLocation != null) {
                        val currentGeoPoint = GeoPoint(lastLocation.latitude, lastLocation.longitude)
                        mapView.controller.animateTo(currentGeoPoint, SELECTED_ZOOM, 1000L)
                        mapView.postDelayed({
                            updateMarkerAndAddress()
                        }, MAP_ANIMATION_DELAY_MS)
                    } else {
                        Toast.makeText(this, getString(R.string.could_not_get_location), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            isRequestingLocation = false
            Toast.makeText(this, getString(R.string.could_not_get_location), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAddressFromLocation(geoPoint: GeoPoint) {
        // Update coordinates display
        binding.tvCoordinates.text = String.format(
            Locale.US, 
            "Lat: %.6f, Lng: %.6f", 
            geoPoint.latitude, 
            geoPoint.longitude
        )

        // Perform reverse geocoding to get address
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
            
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                selectedAddress = address.getAddressLine(0) ?: ""
                selectedCityName = address.locality ?: address.subAdminArea ?: address.adminArea ?: ""
                selectedPostalCode = address.postalCode ?: ""
                
                binding.tvSelectedAddress.text = selectedAddress
                
                // Enable confirm button
                binding.btnConfirm.isEnabled = true
            } else {
                binding.tvSelectedAddress.text = getString(R.string.address_not_found)
                selectedAddress = ""
                selectedCityName = ""
                selectedPostalCode = ""
                binding.btnConfirm.isEnabled = false
            }
        } catch (e: IOException) {
            // Network or I/O error during geocoding
            binding.tvSelectedAddress.text = getString(R.string.error_getting_address)
            selectedAddress = ""
            selectedCityName = ""
            selectedPostalCode = ""
            binding.btnConfirm.isEnabled = false
        } catch (e: IllegalArgumentException) {
            // Invalid coordinates
            binding.tvSelectedAddress.text = getString(R.string.error_getting_address)
            selectedAddress = ""
            selectedCityName = ""
            selectedPostalCode = ""
            binding.btnConfirm.isEnabled = false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.location_permission_denied),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        // Stop location updates when pausing
        locationManager?.removeUpdates(locationListener)
        isRequestingLocation = false
    }
    
    override fun onDestroy() {
        super.onDestroy()
        locationManager?.removeUpdates(locationListener)
    }
}
