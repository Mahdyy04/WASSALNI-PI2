# 🗺️ Guide d'Intégration de la Géolocalisation - Wssalni

Ce guide explique comment intégrer la géolocalisation dans les fonctionnalités **Publish Ride** (publication de trajet) et **Booking** (réservation) de l'application Android Wssalni.

---

## 📋 Vue d'ensemble

### Objectifs
1. **Publish Ride** : Permettre au conducteur de sélectionner les points de départ et d'arrivée sur une carte OpenStreetMap
2. **Search/Booking** : Afficher les trajets disponibles sur une carte et permettre aux passagers de voir la route

### Technologies utilisées
- **OSMDroid** pour Android (équivalent de Leaflet.js pour le web)
- **OpenStreetMap** pour les tuiles de carte (gratuit, sans API key)
- **Android Geocoder** pour le géocodage inverse

### ✅ Avantages de OSMDroid vs Google Maps
| Critère | OSMDroid (Leaflet) | Google Maps |
|---------|-------------------|-------------|
| Coût | **Gratuit** | Payant après quota |
| API Key | **Non requise** | Obligatoire |
| Open Source | **Oui** | Non |
| Offline | **Supporté** | Limité |
| Personnalisation | **Haute** | Moyenne |

---

## 🔧 Étape 1 : Configuration du Projet

### 1.1 Ajouter les dépendances dans `build.gradle.kts` (app)

```kotlin
dependencies {
    // ... dépendances existantes ...
    
    // OSMDroid (OpenStreetMap for Android) - Équivalent de Leaflet.js
    // Gratuit, sans API key requise
    implementation("org.osmdroid:osmdroid-android:6.1.18")
}
```

### 1.2 Ajouter les permissions dans `AndroidManifest.xml`

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
    
    <!-- Permissions existantes -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <!-- Permissions pour la géolocalisation -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    
    <!-- Permission pour le cache des tuiles OSMDroid (Android < 10) -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
        android:maxSdkVersion="28"
        tools:ignore="ScopedStorage" />

    <application ...>
        <!-- Activer l'accélération matérielle pour les cartes -->
        <activity
            android:name=".LocationPickerActivity"
            android:hardwareAccelerated="true" />
    </application>
</manifest>
```

> **Note** : Contrairement à Google Maps, OSMDroid ne nécessite **aucune clé API** !

---

## 🗺️ Étape 2 : Créer l'Activité de Sélection de Localisation

### 2.1 Créer le layout `activity_location_picker.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- OSMDroid MapView (OpenStreetMap) -->
    <org.osmdroid.views.MapView
        android:id="@+id/mapView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toTopOf="@id/bottomCard" />

    <!-- Marqueur central -->
    <ImageView
        android:id="@+id/ivCenterMarker"
        android:layout_width="48dp"
        android:layout_height="48dp"
        android:src="@android:drawable/ic_menu_mylocation"
        android:contentDescription="Selected location"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toTopOf="@id/mapView"
        app:layout_constraintBottom_toBottomOf="@id/mapView" />

    <!-- Bouton Ma Position -->
    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/btnMyLocation"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:src="@android:drawable/ic_menu_mylocation"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintBottom_toTopOf="@id/bottomCard" />

    <!-- Carte du bas avec adresse et boutons -->
    <com.google.android.material.card.MaterialCardView
        android:id="@+id/bottomCard"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:cardElevation="8dp"
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
                android:textStyle="bold" />

            <TextView
                android:id="@+id/tvCoordinates"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Lat: --, Lng: --"
                android:textSize="12sp"
                android:layout_marginBottom="16dp" />

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal">

                <com.google.android.material.button.MaterialButton
                    android:id="@+id/btnCancel"
                    style="@style/Widget.Material3.Button.OutlinedButton"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:layout_marginEnd="8dp"
                    android:text="Annuler" />

                <com.google.android.material.button.MaterialButton
                    android:id="@+id/btnConfirm"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:text="Confirmer"
                    android:enabled="false" />
            </LinearLayout>
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

</androidx.constraintlayout.widget.ConstraintLayout>
```

### 2.2 Créer `LocationPickerActivity.kt`

```kotlin
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
import java.util.Locale

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
        
        // Position par défaut : Tunis, Tunisie
        private val DEFAULT_LOCATION = GeoPoint(36.8065, 10.1815)
        private const val DEFAULT_ZOOM = 12.0
        private const val SELECTED_ZOOM = 15.0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configurer OSMDroid AVANT d'inflater le layout
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName
        
        binding = ActivityLocationPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupMap()
        setupListeners()
    }

    private fun setupMap() {
        mapView = binding.mapView
        
        // Configurer la carte
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(DEFAULT_ZOOM)
        mapView.controller.setCenter(DEFAULT_LOCATION)
        
        // Créer le marqueur central
        centerMarker = Marker(mapView).apply {
            position = DEFAULT_LOCATION
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        mapView.overlays.add(centerMarker)
        
        // Écouter les mouvements de carte
        mapView.setOnTouchListener { _, _ ->
            mapView.postDelayed({
                updateMarkerAndAddress()
            }, 100)
            false
        }
        
        // Essayer d'obtenir la position actuelle
        checkLocationPermissionAndGetLocation()
    }

    private fun updateMarkerAndAddress() {
        val center = mapView.mapCenter as GeoPoint
        selectedGeoPoint = center
        
        // Mettre à jour la position du marqueur
        centerMarker?.position = center
        mapView.invalidate()
        
        // Mettre à jour l'adresse via géocodage inverse
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
            }
        }

        binding.btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
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

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        
        location?.let {
            val currentGeoPoint = GeoPoint(it.latitude, it.longitude)
            mapView.controller.animateTo(currentGeoPoint, SELECTED_ZOOM, 1000L)
        }
    }

    private fun updateAddressFromLocation(geoPoint: GeoPoint) {
        // Afficher les coordonnées
        binding.tvCoordinates.text = String.format(
            Locale.US, 
            "Lat: %.6f, Lng: %.6f", 
            geoPoint.latitude, 
            geoPoint.longitude
        )

        // Géocodage inverse
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
            
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                selectedAddress = address.getAddressLine(0) ?: ""
                selectedCityName = address.locality ?: address.subAdminArea ?: ""
                selectedPostalCode = address.postalCode ?: ""
                
                binding.tvSelectedAddress.text = selectedAddress
                binding.btnConfirm.isEnabled = true
            }
        } catch (e: Exception) {
            binding.tvSelectedAddress.text = "Adresse non trouvée"
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
```

---

## 📝 Étape 3 : Mise à jour du Modèle City

Le modèle `City` a été mis à jour pour inclure les coordonnées GPS :

```kotlin
data class City(
    val name: String = "",
    val postalCode: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)
```

---

## 🎯 Étape 4 : Intégration dans PublishRideFragment

Le fragment a été modifié pour inclure des boutons "Select on Map" qui ouvrent le picker de localisation :

```kotlin
// Dans PublishRideFragment.kt

// Launcher pour le résultat
private val departureLocationLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        result.data?.let { data ->
            departureLatitude = data.getDoubleExtra(LocationPickerActivity.RESULT_LATITUDE, 0.0)
            departureLongitude = data.getDoubleExtra(LocationPickerActivity.RESULT_LONGITUDE, 0.0)
            val cityName = data.getStringExtra(LocationPickerActivity.RESULT_CITY_NAME) ?: ""
            binding.etDepartureCity.setText(cityName)
        }
    }
}

// Pour ouvrir le picker
binding.btnPickDeparture.setOnClickListener {
    val intent = Intent(requireContext(), LocationPickerActivity::class.java)
    intent.putExtra(LocationPickerActivity.EXTRA_LOCATION_TYPE, "departure")
    departureLocationLauncher.launch(intent)
}
```

---

## 📋 Résumé des Fichiers Modifiés

| Fichier | Modification |
|---------|-------------|
| `build.gradle.kts` | Ajout de `osmdroid-android:6.1.18` |
| `AndroidManifest.xml` | Permissions location, suppression Google Maps API |
| `LocationPickerActivity.kt` | Nouvelle activité avec OSMDroid |
| `activity_location_picker.xml` | Layout avec `org.osmdroid.views.MapView` |
| `Ride.kt` | Ajout de `latitude/longitude` au modèle `City` |
| `PublishRideFragment.kt` | Intégration des boutons de sélection |
| `fragment_publish_ride.xml` | Boutons "Select on Map" |

---

## ✅ Avantages de cette Implementation

1. **Gratuit** - Aucun coût pour les tuiles OpenStreetMap
2. **Sans API Key** - Pas besoin de configuration Google Cloud
3. **Open Source** - Communauté active, code transparent
4. **Similaire à Leaflet** - Même philosophie que Leaflet.js pour le web
5. **Léger** - Dépendance unique (~2MB)
6. **Offline Support** - Possibilité de mettre en cache les tuiles

---

## 🔗 Ressources

- [OSMDroid GitHub](https://github.com/osmdroid/osmdroid)
- [OSMDroid Wiki](https://github.com/osmdroid/osmdroid/wiki)
- [OpenStreetMap](https://www.openstreetmap.org/)
- [Leaflet.js](https://leafletjs.com/) (équivalent web)
