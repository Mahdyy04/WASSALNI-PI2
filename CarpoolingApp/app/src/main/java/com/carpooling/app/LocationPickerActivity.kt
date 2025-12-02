package com.carpooling.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
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

    companion object {
        const val EXTRA_LOCATION_TYPE = "location_type"
        const val RESULT_LATITUDE = "latitude"
        const val RESULT_LONGITUDE = "longitude"
        const val RESULT_ADDRESS = "address"
        const val RESULT_CITY_NAME = "city_name"
        const val RESULT_POSTAL_CODE = "postal_code"
        private const val LOCATION_PERMISSION_REQUEST = 1001
        private const val MAP_ANIMATION_DELAY_MS = 1100L
        
        // Default location: Tunis, Tunisia
        private val DEFAULT_LOCATION = GeoPoint(36.8065, 10.1815)
        private const val DEFAULT_ZOOM = 12.0
        private const val SELECTED_ZOOM = 15.0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure OSMDroid before inflating layout
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName
        
        binding = ActivityLocationPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        
        // Try to get current location
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
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            
            location?.let {
                val currentGeoPoint = GeoPoint(it.latitude, it.longitude)
                mapView.controller.animateTo(currentGeoPoint, SELECTED_ZOOM, 1000L)
                mapView.postDelayed({
                    updateMarkerAndAddress()
                }, MAP_ANIMATION_DELAY_MS)
            } ?: run {
                Toast.makeText(this, getString(R.string.could_not_get_location), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
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
    }
}
