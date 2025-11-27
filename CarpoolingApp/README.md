# Carpooling Android Application

A native Android application built with Kotlin for the carpooling system, following Android best practices with XML layouts and proper project structure.

## Features

- 🔐 **Authentication System**
  - User Login with email and password
  - User Signup with role selection (Passenger/Driver)
  - Session management with SharedPreferences
  
- 👥 **Passenger Features**
  - Search for available rides
  - View ride details (price, seats, driver info)
  - Book rides instantly
  - View and manage bookings
  
- 🚗 **Driver Features**
  - All passenger features
  - Publish new rides
  - Manage published rides

- 🎨 **Modern UI**
  - Material Design 3 components
  - XML-based layouts
  - ViewBinding for type-safe view access
  - Tab-based navigation
  - RecyclerView for efficient list rendering

## Tech Stack

- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Architecture**: MVVM-ready structure
- **UI**: XML layouts with Material Design 3
- **Networking**: Retrofit 2 + OkHttp 3
- **Coroutines**: For asynchronous operations
- **Dependency Injection**: Manual (can be upgraded to Hilt/Dagger)

## Project Structure

```
CarpoolingApp/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/carpooling/app/
│   │       │   ├── adapters/          # RecyclerView adapters
│   │       │   │   └── RideAdapter.kt
│   │       │   ├── api/               # API service interfaces
│   │       │   │   └── ApiService.kt
│   │       │   ├── fragments/         # UI fragments
│   │       │   │   └── SearchRidesFragment.kt
│   │       │   ├── models/            # Data models
│   │       │   │   ├── User.kt
│   │       │   │   ├── Ride.kt
│   │       │   │   └── LoginRequest.kt
│   │       │   ├── network/           # Networking setup
│   │       │   │   └── RetrofitClient.kt
│   │       │   ├── utils/             # Utility classes
│   │       │   │   └── SessionManager.kt
│   │       │   ├── MainActivity.kt
│   │       │   ├── LoginActivity.kt
│   │       │   ├── SignupActivity.kt
│   │       │   └── DashboardActivity.kt
│   │       ├── res/
│   │       │   ├── layout/            # XML layouts
│   │       │   │   ├── activity_main.xml
│   │       │   │   ├── activity_login.xml
│   │       │   │   ├── activity_signup.xml
│   │       │   │   ├── activity_dashboard.xml
│   │       │   │   ├── fragment_search_rides.xml
│   │       │   │   └── item_ride.xml
│   │       │   ├── values/            # Resources
│   │       │   │   ├── strings.xml
│   │       │   │   ├── colors.xml
│   │       │   │   └── themes.xml
│   │       │   └── xml/               # App config
│   │       │       ├── backup_rules.xml
│   │       │       └── data_extraction_rules.xml
│   │       └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android SDK with API 34
- Backend microservices running (see Backend Setup)

### Installation

1. **Clone the repository** (already done if you're reading this)

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the `CarpoolingApp` folder
   - Click "OK"

3. **Sync Gradle**
   - Android Studio should automatically sync Gradle
   - If not, click "File" → "Sync Project with Gradle Files"

4. **Configure Backend URL**
   - Open `app/src/main/java/com/carpooling/app/network/RetrofitClient.kt`
   - Update `BASE_URL`:
     - For Android Emulator: `http://10.0.2.2:8084/`
     - For Physical Device: `http://<YOUR_COMPUTER_IP>:8084/`

5. **Run the App**
   - Connect an Android device or start an emulator
   - Click the "Run" button (green triangle) or press `Shift + F10`
   - Select your device and click "OK"

## Backend Setup

The app connects to the following microservices:

- **Gateway Service**: Port 8084 (Main entry point)
- **Authentication Service**: Port 8081
- **Ride Service**: Port 8085
- **Booking Service**: Port 8082

### Starting Backend Services

1. Navigate to each microservice directory:
   ```bash
   cd ../microservices/gateway-service\(8084\)
   mvn spring-boot:run
   ```

2. Repeat for other services (authentication, ride, booking)

3. Verify services are running:
   - Gateway: http://localhost:8084/actuator/health
   - Auth: http://localhost:8081/actuator/health

## Usage

### Demo Mode

The app includes demo/fallback mode that works without a backend:
- Login with any email/password
- Signup with any credentials
- Sample rides are displayed
- All features are functional in demo mode

### With Backend

When backend services are running:
- Real authentication against the auth service
- Actual ride data from the database
- Real booking operations
- Persistent data

## API Integration

### Authentication Endpoints

```kotlin
POST /auth/login
Body: { "email": "user@example.com", "password": "password" }
Response: { "id": "1", "name": "John", "email": "...", "role": "PASSENGER", "token": "..." }

POST /auth/signup  
Body: { "name": "John", "email": "...", "password": "...", "role": "PASSENGER" }
Response: User object with token
```

### Ride Endpoints

```kotlin
GET /rides/search?from=CityA&to=CityB&date=2025-12-01
Response: [ { "id": "1", "from": "CityA", ... }, ... ]

POST /rides
Body: { "from": "CityA", "to": "CityB", "date": "...", ... }
Response: Created ride object

GET /bookings
Response: List of user's bookings

POST /bookings/{rideId}
Response: Booking confirmation
```

## Building for Release

1. **Generate Signed APK**
   - Build → Generate Signed Bundle/APK
   - Select "APK"
   - Create or select a keystore
   - Fill in key details
   - Select "release" build variant
   - Click "Finish"

2. **Location of APK**
   - `app/release/app-release.apk`

## Development Tips

- **ViewBinding**: Enabled by default, provides type-safe view access
- **Coroutines**: Used for async operations, always run on `lifecycleScope`
- **Error Handling**: Demo mode falls back when API calls fail
- **Logging**: HTTP logging interceptor is enabled in debug builds

## Troubleshooting

### Common Issues

1. **Cannot connect to backend**
   - Check if backend services are running
   - Verify BASE_URL in RetrofitClient.kt
   - For emulator, use `10.0.2.2` instead of `localhost`
   - Ensure `android:usesCleartextTraffic="true"` in AndroidManifest.xml

2. **Gradle sync failed**
   - File → Invalidate Caches / Restart
   - Delete `.gradle` and `.idea` folders
   - Sync again

3. **App crashes on startup**
   - Check Logcat for error messages
   - Verify all dependencies are downloaded
   - Clean and rebuild project

## Future Enhancements

- [ ] Add pull-to-refresh functionality
- [ ] Implement real-time notifications
- [ ] Add maps integration for route visualization
- [ ] Implement reviews and ratings system
- [ ] Add photo upload for user profiles
- [ ] Implement chat between drivers and passengers
- [ ] Add payment integration
- [ ] Implement admin panel features
- [ ] Add unit and instrumentation tests
- [ ] Migrate to Jetpack Compose (optional)

## License

This project is part of the Carpooling Application system.

## Support

For issues or questions about the Android app, check the main repository documentation or contact the development team.
