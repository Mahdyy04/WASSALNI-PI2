# 🗺️ Guide d'Intégration de la Géolocalisation - Wssalni

Ce guide explique comment intégrer la géolocalisation dans les fonctionnalités **Publish Ride** (publication de trajet) et **Booking** (réservation) de l'application Android Wssalni.

---

## 📋 Vue d'ensemble

### Objectifs
1. **Publish Ride** : Permettre au conducteur de sélectionner les points de départ et d'arrivée sur une carte Google Maps
2. **Search/Booking** : Afficher les trajets disponibles sur une carte et permettre aux passagers de voir la route

### Technologies à utiliser
- **Google Maps SDK** pour Android
- **Google Places API** pour l'autocomplétion d'adresses
- **FusedLocationProviderClient** pour la localisation de l'utilisateur

---

## 🔧 Étape 1 : Configuration du Projet

### 1.1 Ajouter les dépendances dans `build.gradle.kts` (app)

```kotlin
dependencies {
    // ... dépendances existantes ...
    
    // Google Maps SDK (version stable)
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    
    // Google Location Services (pour la localisation)
    implementation("com.google.android.gms:play-services-location:21.3.0")
    
    // Google Places API (pour l'autocomplétion d'adresses)
    implementation("com.google.android.libraries.places:places:4.1.0")
}
```

> **Note** : Ces versions sont les dernières stables disponibles (décembre 2024). Vérifiez toujours les dernières versions sur [Maven Repository](https://mvnrepository.com/artifact/com.google.android.gms) avant d'implémenter.

### 1.2 Ajouter les permissions dans `AndroidManifest.xml`

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- Permissions existantes -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <!-- NOUVELLES Permissions pour la géolocalisation -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    
    <application
        ...>
        
        <!-- Clé API Google Maps (voir section 1.3 pour la sécurité) -->
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="${MAPS_API_KEY}" />
            
        <!-- Activités existantes ... -->
    </application>
</manifest>
```

> ⚠️ **Sécurité** : Ne jamais commiter votre clé API directement dans le code. Utilisez des variables d'environnement ou le fichier `local.properties` :
> 
> Dans `local.properties` (fichier ignoré par git) :
> ```properties
> MAPS_API_KEY=AIzaSy...votre_cle_api
> ```
> 
> Dans `build.gradle.kts` :
> ```kotlin
> android {
>     defaultConfig {
>         manifestPlaceholders["MAPS_API_KEY"] = project.findProperty("MAPS_API_KEY") ?: ""
>     }
> }
> ```

### 1.3 Obtenir une clé API Google Maps

1. Allez sur [Google Cloud Console](https://console.cloud.google.com/)
2. Créez un nouveau projet ou sélectionnez un projet existant
3. Activez les APIs suivantes :
   - Maps SDK for Android
   - Places API
   - Geocoding API
4. Créez des credentials (API Key)
5. Restreignez la clé à votre application Android (SHA-1 + package name)

---

## 🏗️ Étape 2 : Mise à jour des Modèles de Données

### 2.1 Modifier le modèle `City` dans `Ride.kt`

```kotlin
package com.carpooling.app.models

/**
 * City model avec coordonnées GPS
 * Note: Les champs latitude/longitude sont optionnels pour maintenir 
 * la compatibilité avec le backend existant
 */
data class City(
    val name: String = "",
    val postalCode: String = "",
    // NOUVEAUX champs optionnels pour la géolocalisation
    val latitude: Double? = null,
    val longitude: Double? = null
)
```

> ⚠️ **Important** : Assurez-vous que le backend supporte ces nouveaux champs. Si le backend n'est pas modifié, les coordonnées seront ignorées lors de la sérialisation JSON grâce à leur caractère nullable.

### 2.2 Modifier `CreateRideRequest` dans `LoginRequest.kt`

```kotlin
/**
 * Request model pour créer un trajet avec coordonnées GPS
 */
data class CreateRideRequest(
    val departureCity: City,
    val destinationCity: City,
    val departureDate: String,
    val availableSeats: Int,
    val pricePerSeat: Double,
    val driverId: String
)
```

---

## 🗺️ Étape 3 : Créer l'Activité de Sélection de Localisation

### 3.1 Créer le layout `activity_location_picker.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- Barre de recherche d'adresse -->
    <com.google.android.material.card.MaterialCardView
        android:id="@+id/searchCard"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        app:cardElevation="8dp"
        app:cardCornerRadius="8dp"
        app:layout_constraintTop_toTopOf="parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="8dp">

            <ImageView
                android:layout_width="24dp"
                android:layout_height="24dp"
                android:layout_gravity="center_vertical"
                android:src="@android:drawable/ic_menu_search" />

            <EditText
                android:id="@+id/etSearchAddress"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:hint="Rechercher une adresse..."
                android:background="@null"
                android:padding="8dp" />

        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

    <!-- Carte Google Maps -->
    <fragment
        android:id="@+id/mapFragment"
        android:name="com.google.android.gms.maps.SupportMapFragment"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        app:layout_constraintTop_toBottomOf="@id/searchCard"
        app:layout_constraintBottom_toTopOf="@id/bottomCard" />

    <!-- Marqueur central (point de sélection) -->
    <ImageView
        android:id="@+id/ivCenterMarker"
        android:layout_width="48dp"
        android:layout_height="48dp"
        android:src="@android:drawable/ic_menu_mylocation"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toTopOf="@id/mapFragment"
        app:layout_constraintBottom_toBottomOf="@id/mapFragment" />

    <!-- Carte du bas avec adresse sélectionnée et bouton confirmer -->
    <com.google.android.material.card.MaterialCardView
        android:id="@+id/bottomCard"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:cardElevation="8dp"
        app:cardCornerRadius="16dp"
        app:layout_constraintBottom_toBottomOf="parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <TextView
                android:id="@+id/tvSelectedAddress"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Déplacez la carte pour sélectionner"
                android:textSize="16sp"
                android:textColor="@color/text_primary"
                android:layout_marginBottom="8dp" />

            <TextView
                android:id="@+id/tvCoordinates"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Lat: --, Lng: --"
                android:textSize="12sp"
                android:textColor="@color/text_secondary"
                android:layout_marginBottom="16dp" />

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal">

                <com.google.android.material.button.MaterialButton
                    android:id="@+id/btnMyLocation"
                    style="@style/Widget.Material3.Button.OutlinedButton"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:layout_marginEnd="8dp"
                    android:text="Ma position" />

                <com.google.android.material.button.MaterialButton
                    android:id="@+id/btnConfirm"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="Confirmer" />

            </LinearLayout>
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

</androidx.constraintlayout.widget.ConstraintLayout>
```

### 3.2 Créer `LocationPickerActivity.kt`

```kotlin
package com.carpooling.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.carpooling.app.databinding.ActivityLocationPickerBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class LocationPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityLocationPickerBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedLatLng: LatLng? = null
    private var selectedAddress: String = ""

    companion object {
        const val EXTRA_LOCATION_TYPE = "location_type" // "departure" ou "destination"
        const val RESULT_LATITUDE = "latitude"
        const val RESULT_LONGITUDE = "longitude"
        const val RESULT_ADDRESS = "address"
        const val RESULT_CITY_NAME = "city_name"
        const val RESULT_POSTAL_CODE = "postal_code"
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialiser la carte
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

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
                    // Extraire le nom de la ville et le code postal
                    val geocoder = Geocoder(this@LocationPickerActivity, Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocation(
                            selectedLatLng!!.latitude, 
                            selectedLatLng!!.longitude, 
                            1
                        )
                        if (!addresses.isNullOrEmpty()) {
                            putExtra(RESULT_CITY_NAME, addresses[0].locality ?: addresses[0].subAdminArea ?: "")
                            putExtra(RESULT_POSTAL_CODE, addresses[0].postalCode ?: "")
                        }
                    } catch (e: Exception) {
                        // Utiliser l'adresse complète si le geocoding échoue
                    }
                }
                setResult(RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(this, "Veuillez sélectionner une position", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Configuration de la carte
        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
        }

        // Position par défaut (Tunis, Tunisie)
        val defaultLocation = LatLng(36.8065, 10.1815)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))

        // Écouter les mouvements de la caméra
        // Note: Pour éviter trop d'appels API, on utilise setOnCameraIdleListener
        // qui ne se déclenche que quand l'utilisateur arrête de bouger la carte
        googleMap.setOnCameraIdleListener {
            val center = googleMap.cameraPosition.target
            selectedLatLng = center
            updateAddressFromLocation(center)
        }

        // Obtenir la position actuelle si permission accordée
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            }
        }
    }

    private fun updateAddressFromLocation(latLng: LatLng) {
        binding.tvCoordinates.text = "Lat: ${String.format("%.6f", latLng.latitude)}, Lng: ${String.format("%.6f", latLng.longitude)}"

        // Géocoding inverse pour obtenir l'adresse
        // Note: getFromLocation() est déprécié à partir de API 33, 
        // mais reste fonctionnel pour les versions antérieures
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!addresses.isNullOrEmpty() && addresses.size > 0) {
                val address = addresses[0]
                selectedAddress = address.getAddressLine(0) ?: ""
                binding.tvSelectedAddress.text = selectedAddress
            } else {
                binding.tvSelectedAddress.text = "Adresse inconnue"
            }
        } catch (e: Exception) {
            binding.tvSelectedAddress.text = "Erreur de géocoding"
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
            }
        }
    }
}
```

---

## 📝 Étape 4 : Modifier PublishRideFragment pour la Géolocalisation

### 4.1 Mettre à jour le layout `fragment_publish_ride.xml`

Ajoutez des boutons pour ouvrir le sélecteur de carte :

```xml
<!-- Après le champ Departure City -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/btnPickDepartureLocation"
    style="@style/Widget.Material3.Button.OutlinedButton"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="📍 Sélectionner sur la carte"
    android:layout_marginBottom="8dp" />

<!-- Après le champ Destination City -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/btnPickDestinationLocation"
    style="@style/Widget.Material3.Button.OutlinedButton"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="📍 Sélectionner sur la carte"
    android:layout_marginBottom="16dp" />
```

### 4.2 Modifier `PublishRideFragment.kt`

```kotlin
package com.carpooling.app.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.carpooling.app.LocationPickerActivity
import com.carpooling.app.R
import com.carpooling.app.databinding.FragmentPublishRideBinding
import com.carpooling.app.models.City
import com.carpooling.app.models.CreateRideRequest
import com.carpooling.app.network.RetrofitClient
import com.carpooling.app.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PublishRideFragment : Fragment() {

    private var _binding: FragmentPublishRideBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Variables pour stocker les coordonnées sélectionnées
    private var departureLatitude: Double? = null
    private var departureLongitude: Double? = null
    private var destinationLatitude: Double? = null
    private var destinationLongitude: Double? = null

    // Launcher pour le résultat de LocationPickerActivity (Départ)
    private val departureLocationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                departureLatitude = data.getDoubleExtra(LocationPickerActivity.RESULT_LATITUDE, 0.0)
                departureLongitude = data.getDoubleExtra(LocationPickerActivity.RESULT_LONGITUDE, 0.0)
                val cityName = data.getStringExtra(LocationPickerActivity.RESULT_CITY_NAME) ?: ""
                val postalCode = data.getStringExtra(LocationPickerActivity.RESULT_POSTAL_CODE) ?: ""
                
                binding.etDepartureCity.setText(cityName)
                binding.etDeparturePostal.setText(postalCode)
                
                Toast.makeText(context, "Point de départ sélectionné ✓", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher pour le résultat de LocationPickerActivity (Destination)
    private val destinationLocationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                destinationLatitude = data.getDoubleExtra(LocationPickerActivity.RESULT_LATITUDE, 0.0)
                destinationLongitude = data.getDoubleExtra(LocationPickerActivity.RESULT_LONGITUDE, 0.0)
                val cityName = data.getStringExtra(LocationPickerActivity.RESULT_CITY_NAME) ?: ""
                val postalCode = data.getStringExtra(LocationPickerActivity.RESULT_POSTAL_CODE) ?: ""
                
                binding.etDestinationCity.setText(cityName)
                binding.etDestinationPostal.setText(postalCode)
                
                Toast.makeText(context, "Destination sélectionnée ✓", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPublishRideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        setupDatePicker()
        setupLocationPickers()

        binding.btnPublish.setOnClickListener {
            if (validateInput()) {
                publishRide()
            }
        }
    }

    private fun setupLocationPickers() {
        // Bouton pour sélectionner le point de départ sur la carte
        binding.btnPickDepartureLocation.setOnClickListener {
            val intent = Intent(requireContext(), LocationPickerActivity::class.java)
            intent.putExtra(LocationPickerActivity.EXTRA_LOCATION_TYPE, "departure")
            departureLocationLauncher.launch(intent)
        }

        // Bouton pour sélectionner la destination sur la carte
        binding.btnPickDestinationLocation.setOnClickListener {
            val intent = Intent(requireContext(), LocationPickerActivity::class.java)
            intent.putExtra(LocationPickerActivity.EXTRA_LOCATION_TYPE, "destination")
            destinationLocationLauncher.launch(intent)
        }
    }

    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    binding.etDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = System.currentTimeMillis() - 1000
            }.show()
        }
    }

    private fun validateInput(): Boolean {
        // ... validation existante ...
        return true
    }

    private fun publishRide() {
        val driverId = sessionManager.getUserId()
        if (driverId.isEmpty()) {
            Toast.makeText(context, "Error: Not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnPublish.isEnabled = false

        // Créer City avec coordonnées GPS
        val departureCity = City(
            name = binding.etDepartureCity.text.toString().trim(),
            postalCode = binding.etDeparturePostal.text.toString().trim(),
            latitude = departureLatitude,
            longitude = departureLongitude
        )
        val destinationCity = City(
            name = binding.etDestinationCity.text.toString().trim(),
            postalCode = binding.etDestinationPostal.text.toString().trim(),
            latitude = destinationLatitude,
            longitude = destinationLongitude
        )

        val request = CreateRideRequest(
            departureCity = departureCity,
            destinationCity = destinationCity,
            departureDate = binding.etDate.text.toString().trim(),
            availableSeats = binding.etSeats.text.toString().toInt(),
            pricePerSeat = binding.etPrice.text.toString().toDouble(),
            driverId = driverId
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.publishRide(request)
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(context, getString(R.string.success_publish), Toast.LENGTH_SHORT).show()
                    clearForm()
                } else {
                    Toast.makeText(context, "Failed to publish ride: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnPublish.isEnabled = true
            }
        }
    }

    private fun clearForm() {
        binding.etDepartureCity.setText("")
        binding.etDeparturePostal.setText("")
        binding.etDestinationCity.setText("")
        binding.etDestinationPostal.setText("")
        binding.etDate.setText("")
        binding.etSeats.setText("")
        binding.etPrice.setText("")
        departureLatitude = null
        departureLongitude = null
        destinationLatitude = null
        destinationLongitude = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

---

## 🗺️ Étape 5 : Afficher la Route dans le Booking

### 5.1 Créer `RideMapActivity.kt` pour afficher la route

```kotlin
package com.carpooling.app

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.carpooling.app.databinding.ActivityRideMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class RideMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityRideMapBinding
    private lateinit var googleMap: GoogleMap

    companion object {
        const val EXTRA_DEPARTURE_LAT = "departure_lat"
        const val EXTRA_DEPARTURE_LNG = "departure_lng"
        const val EXTRA_DEPARTURE_NAME = "departure_name"
        const val EXTRA_DESTINATION_LAT = "destination_lat"
        const val EXTRA_DESTINATION_LNG = "destination_lng"
        const val EXTRA_DESTINATION_NAME = "destination_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRideMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val departureLat = intent.getDoubleExtra(EXTRA_DEPARTURE_LAT, 0.0)
        val departureLng = intent.getDoubleExtra(EXTRA_DEPARTURE_LNG, 0.0)
        val departureName = intent.getStringExtra(EXTRA_DEPARTURE_NAME) ?: "Départ"
        val destinationLat = intent.getDoubleExtra(EXTRA_DESTINATION_LAT, 0.0)
        val destinationLng = intent.getDoubleExtra(EXTRA_DESTINATION_LNG, 0.0)
        val destinationName = intent.getStringExtra(EXTRA_DESTINATION_NAME) ?: "Arrivée"

        if (departureLat != 0.0 && destinationLat != 0.0) {
            val departureLatLng = LatLng(departureLat, departureLng)
            val destinationLatLng = LatLng(destinationLat, destinationLng)

            // Ajouter les marqueurs
            googleMap.addMarker(
                MarkerOptions()
                    .position(departureLatLng)
                    .title(departureName)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )

            googleMap.addMarker(
                MarkerOptions()
                    .position(destinationLatLng)
                    .title(destinationName)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )

            // Tracer une ligne entre les deux points
            googleMap.addPolyline(
                PolylineOptions()
                    .add(departureLatLng, destinationLatLng)
                    .width(8f)
                    .color(Color.BLUE)
                    .geodesic(true)
            )

            // Ajuster la vue pour montrer les deux points
            val bounds = LatLngBounds.Builder()
                .include(departureLatLng)
                .include(destinationLatLng)
                .build()
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
    }
}
```

### 5.2 Modifier `RideAdapter.kt` pour ajouter un bouton "Voir sur la carte"

```kotlin
// Dans RideAdapter.kt, ajouter un bouton pour voir la carte
holder.btnViewMap.setOnClickListener {
    val intent = Intent(context, RideMapActivity::class.java).apply {
        putExtra(RideMapActivity.EXTRA_DEPARTURE_LAT, ride.departureCity.latitude ?: 0.0)
        putExtra(RideMapActivity.EXTRA_DEPARTURE_LNG, ride.departureCity.longitude ?: 0.0)
        putExtra(RideMapActivity.EXTRA_DEPARTURE_NAME, ride.departureCity.name)
        putExtra(RideMapActivity.EXTRA_DESTINATION_LAT, ride.destinationCity.latitude ?: 0.0)
        putExtra(RideMapActivity.EXTRA_DESTINATION_LNG, ride.destinationCity.longitude ?: 0.0)
        putExtra(RideMapActivity.EXTRA_DESTINATION_NAME, ride.destinationCity.name)
    }
    context.startActivity(intent)
}
```

---

## 🔧 Étape 6 : Modifications Backend (Optionnel)

### 6.1 Modifier l'entité `City` dans le backend

```java
// backend/ride-service/src/main/java/com/example/ride/entities/City.java
@Data
public class City {
    private String name;
    private String postalCode;
    // Nouveaux champs
    private Double latitude;
    private Double longitude;
}
```

---

## 📱 Étape 7 : Enregistrer l'Activité dans le Manifest

```xml
<activity
    android:name=".LocationPickerActivity"
    android:exported="false"
    android:label="Sélectionner une position" />

<activity
    android:name=".RideMapActivity"
    android:exported="false"
    android:label="Voir le trajet" />
```

---

## 🎯 Résumé des Fichiers à Modifier/Créer

### Fichiers à CRÉER :
1. `app/src/main/res/layout/activity_location_picker.xml`
2. `app/src/main/java/com/carpooling/app/LocationPickerActivity.kt`
3. `app/src/main/res/layout/activity_ride_map.xml`
4. `app/src/main/java/com/carpooling/app/RideMapActivity.kt`

### Fichiers à MODIFIER :
1. `app/build.gradle.kts` - Ajouter dépendances Google Maps
2. `app/src/main/AndroidManifest.xml` - Ajouter permissions et clé API
3. `app/src/main/java/.../models/Ride.kt` - Ajouter latitude/longitude au City
4. `app/src/main/res/layout/fragment_publish_ride.xml` - Ajouter boutons carte
5. `app/src/main/java/.../fragments/PublishRideFragment.kt` - Intégrer le picker
6. `app/src/main/java/.../adapters/RideAdapter.kt` - Ajouter bouton voir carte
7. `app/src/main/res/layout/item_ride.xml` - Ajouter bouton carte

---

## 🚀 Ordre d'Implémentation Recommandé

1. **Configurer Google Maps** (dépendances, API key, permissions)
2. **Mettre à jour les modèles** (City avec lat/lng)
3. **Créer LocationPickerActivity** (sélecteur de carte)
4. **Modifier PublishRideFragment** (intégrer le picker)
5. **Créer RideMapActivity** (visualisation route)
6. **Modifier RideAdapter** (bouton voir carte)
7. **Tester l'intégration complète**

---

## 📋 Checklist de Test

- [ ] La clé API Google Maps est configurée et fonctionne
- [ ] L'utilisateur peut ouvrir le picker de carte depuis Publish Ride
- [ ] La position actuelle de l'utilisateur est affichée
- [ ] L'utilisateur peut sélectionner un point sur la carte
- [ ] Le géocoding inverse affiche l'adresse correctement
- [ ] Les coordonnées sont sauvegardées avec le trajet
- [ ] Les passagers peuvent voir la route sur une carte
- [ ] Les marqueurs de départ et d'arrivée s'affichent correctement

---

## 🔗 Ressources Utiles

- [Google Maps SDK for Android](https://developers.google.com/maps/documentation/android-sdk)
- [Google Places API](https://developers.google.com/maps/documentation/places/android-sdk)
- [Geocoding API](https://developers.google.com/maps/documentation/geocoding)
- [FusedLocationProviderClient](https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient)
