package com.carpooling.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.carpooling.app.databinding.ActivityLocationPickerBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import java.io.IOException
import java.util.Locale

/**
 * Activity for picking a location on the map.
 * The user can move the map to select a location, and the address will be displayed in real-time.
 */
class LocationPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityLocationPickerBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null
    private var selectedLatLng: LatLng? = null
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
        
        // Default location: Tunis, Tunisia
        private val DEFAULT_LOCATION = LatLng(36.8065, 10.1815)
        private const val DEFAULT_ZOOM = 12f
        private const val SELECTED_ZOOM = 15f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Set title based on location type
        val locationType = intent.getStringExtra(EXTRA_LOCATION_TYPE) ?: "location"
        title = if (locationType == "departure") {
            getString(R.string.select_departure_location)
        } else {
            getString(R.string.select_destination_location)
        }

        // Initialize the map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnMyLocation.setOnClickListener {
            getCurrentLocation()
        }

        binding.btnConfirm.setOnClickListener {
            if (selectedLatLng != null) {
                val intent = Intent().apply {
                    putExtra(RESULT_LATITUDE, selectedLatLng!!.latitude)
                    putExtra(RESULT_LONGITUDE, selectedLatLng!!.longitude)
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

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Configure map UI settings
        map.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMyLocationButtonEnabled = false // We have our own button
        }

        // Move to default location
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM))

        // Set camera idle listener to detect when user stops moving the map
        map.setOnCameraIdleListener {
            val center = map.cameraPosition.target
            selectedLatLng = center
            updateAddressFromLocation(center)
        }

        // Try to get current location
        checkLocationPermissionAndGetLocation()
    }

    private fun checkLocationPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
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

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, SELECTED_ZOOM))
            }
        }.addOnFailureListener {
            Toast.makeText(this, getString(R.string.could_not_get_location), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAddressFromLocation(latLng: LatLng) {
        // Update coordinates display
        binding.tvCoordinates.text = String.format(
            Locale.US, 
            "Lat: %.6f, Lng: %.6f", 
            latLng.latitude, 
            latLng.longitude
        )

        // Perform reverse geocoding to get address
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            
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
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    googleMap?.isMyLocationEnabled = true
                    getCurrentLocation()
                }
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.location_permission_denied),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
