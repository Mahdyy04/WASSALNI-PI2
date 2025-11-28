# Carpooling Android Application

A native Android application built with Kotlin for the carpooling system, following Android best practices with XML layouts and proper project structure.

## Features

- 🔐 **Authentication System**
  - User Login with email and password
  - User Signup with role selection (Passenger/Driver), gender, and phone number
  - Session management with SharedPreferences
  - JWT token support for API authentication
  
- 👥 **Passenger Features**
  - Search for available rides by city and date
  - Filter rides by driver gender (UI ready)
  - View ride details (price, seats, route info)
  - Book rides instantly
  - View and manage bookings with pull-to-refresh
  - Cancel pending bookings
  
- 🚗 **Driver Features**
  - Publish new rides with departure/destination cities
  - Set available seats and price per seat
  - View published rides
  - Accept/Reject booking requests from passengers
  - Delete rides

- ⭐ **Review System** (API ready)
  - Rate drivers and passengers after rides
  - View average ratings
  - Leave comments

- 🚨 **Report System** (API ready)
  - Report inappropriate behavior
  - Track report status

- 🎨 **Modern UI**
  - Material Design 3 components
  - XML-based layouts
  - ViewBinding for type-safe view access
  - Tab-based navigation
  - RecyclerView for efficient list rendering
  - Pull-to-refresh for bookings

## Tech Stack

- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Architecture**: MVVM-ready structure
- **UI**: XML layouts with Material Design 3
- **Networking**: Retrofit 2 + OkHttp 4
- **Coroutines**: For asynchronous operations

## Backend Integration

This app connects to microservices via Spring Cloud Gateway with Eureka service discovery.

### Gateway Routing

The Gateway uses Eureka discovery with the pattern `/{service-name}/api/{endpoint}`:

| Service | Service Name | Port | Gateway Route |
|---------|-------------|------|---------------|
| Gateway | gateway-service | 8084 | Main entry point |
| Authentication | authentication-service | 8081 | `/authentication-service/api/auth/*` |
| Rides | ride-service | 8085 | `/ride-service/api/rides/*` |
| Bookings | booking-service | 8082 | `/booking-service/api/bookings/*` |
| Reviews | review-service | 8086 | `/review-service/api/reviews/*` |
| Reports | report-service | 8087 | `/report-service/api/reports/*` |
| Eureka | eureka-server | 8083 | Service discovery |

### Example API Calls

```
# Authentication
POST http://localhost:8084/authentication-service/api/auth/authenticate
POST http://localhost:8084/authentication-service/api/auth/createAccount

# Rides
GET http://localhost:8084/ride-service/api/rides
GET http://localhost:8084/ride-service/api/rides/search?departureCity=Tunis
POST http://localhost:8084/ride-service/api/rides/create

# Bookings
POST http://localhost:8084/booking-service/api/bookings/create
GET http://localhost:8084/booking-service/api/bookings/passenger/{passengerId}
POST http://localhost:8084/booking-service/api/bookings/{bookingId}/accept?driverId=xxx
```

## Project Structure

```
CarpoolingApp/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/carpooling/app/
│   │       │   ├── adapters/          # RecyclerView adapters
│   │       │   │   ├── RideAdapter.kt
│   │       │   │   ├── BookingAdapter.kt
│   │       │   │   ├── DriverRideAdapter.kt
│   │       │   │   └── PendingBookingAdapter.kt
│   │       │   ├── api/               # API service interfaces
│   │       │   │   └── ApiService.kt
│   │       │   ├── fragments/         # UI fragments
│   │       │   │   ├── SearchRidesFragment.kt
│   │       │   │   ├── MyBookingsFragment.kt
│   │       │   │   ├── MyRidesFragment.kt
│   │       │   │   ├── PublishRideFragment.kt
│   │       │   │   └── PendingBookingsFragment.kt
│   │       │   ├── models/            # Data models
│   │       │   │   ├── User.kt
│   │       │   │   ├── Ride.kt
│   │       │   │   └── LoginRequest.kt  # Contains all DTOs
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
│   │       │   └── values/            # Resources
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android SDK with API 34
- Backend microservices running (see Backend Setup)

### Installation

1. **Clone the repository**

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

### Starting Backend Services

Start services in this order:

1. **Eureka Server** (8083) - Service discovery must start first
   ```bash
   cd backend/eureka-server\(8083\)
   mvn spring-boot:run
   ```

2. **Gateway Service** (8084)
   ```bash
   cd backend/gateway-service\(8084\)
   mvn spring-boot:run
   ```

3. **Microservices** (any order)
   ```bash
   cd backend/authentication-service\(8081\)
   mvn spring-boot:run
   
   cd backend/ride-service\(8085\)
   mvn spring-boot:run
   
   cd backend/booking-service\(8082\)
   mvn spring-boot:run
   ```

4. **Verify services are registered** in Eureka:
   - Open http://localhost:8083
   - Check that all services appear in the instances list

### API Endpoints

#### Authentication Endpoints

```
POST /authentication-service/api/auth/authenticate
Body: { "email": "user@example.com", "password": "password" }
Response: { "user": {...}, "token": "jwt-token", "status": "success" }

POST /authentication-service/api/auth/createAccount
Body: { 
  "email": "...", 
  "password": "...", 
  "phoneNumber": "...",
  "gender": "MALE|FEMALE",
  "userType": "PASSENGER|DRIVER",
  "licenseNumber": "...",  // Driver only
  "vehicleNumber": "...",  // Driver only
  "vehiclePlate": "..."    // Driver only
}
Response: User object
```

#### Ride Endpoints

```
GET /ride-service/api/rides
Response: List of all rides

GET /ride-service/api/rides/search?departureCity=Tunis&destinationCity=Sousse&date=2025-12-01
Response: [ { "id": "1", "departureCity": {...}, "destinationCity": {...}, ... }, ... ]

POST /ride-service/api/rides/create
Body: { 
  "departureCity": { "name": "Tunis", "postalCode": "1000" },
  "destinationCity": { "name": "Sousse", "postalCode": "4000" },
  "departureDate": "2025-12-01",
  "availableSeats": 3,
  "pricePerSeat": 25.0,
  "driverId": "..."
}
Response: Created ride object

GET /ride-service/api/rides/driver/{driverId}
Response: List of driver's rides
```

#### Booking Endpoints

```
POST /booking-service/api/bookings/create
Body: { "rideId": "...", "passengerId": "...", "seats": 1 }
Response: BookingResponse object

GET /booking-service/api/bookings/passenger/{passengerId}
Response: List of passenger's bookings

GET /booking-service/api/bookings/driver/{driverId}/pending
Response: List of pending bookings for driver

DELETE /booking-service/api/bookings/{bookingId}?passengerId=...
Response: "Booking canceled"

POST /booking-service/api/bookings/{bookingId}/accept?driverId=...
POST /booking-service/api/bookings/{bookingId}/reject?driverId=...
```

#### Review Endpoints

```
POST /review-service/api/reviews/create
Body: { 
  "reviewerId": "...", 
  "reviewedId": "...", 
  "rideId": "...",
  "rating": 5, 
  "comment": "...",
  "type": "DRIVER|PASSENGER"
}
Response: Review object

GET /review-service/api/reviews/user/{userId}
GET /review-service/api/reviews/user/{userId}/average
```

#### Report Endpoints

```
POST /report-service/api/reports/create
Body: { 
  "reporterId": "...", 
  "reportedUserId": "...", 
  "rideId": "...",
  "reason": "INAPPROPRIATE_BEHAVIOR|NO_SHOW|UNSAFE_DRIVING|OTHER",
  "description": "..."
}
Response: Report object

GET /report-service/api/reports/status/{status}
```

## Role-Based Features

### Passenger Dashboard (2 tabs)
- **Search Rides**: Find available rides with city/date filters and optional gender filter
- **My Bookings**: View and cancel bookings

### Driver Dashboard (3 tabs)
- **My Rides**: View and delete published rides
- **Pending Requests**: Accept/reject booking requests
- **Publish Ride**: Create new rides with identity verification

## Troubleshooting

### Common Issues

1. **404 Not Found errors**
   - Verify all backend services are registered in Eureka (http://localhost:8083)
   - Check that service names match: `authentication-service`, `ride-service`, etc.
   - Ensure gateway routes use the correct pattern: `/{service-name}/api/...`

2. **Cannot connect to backend from emulator**
   - Use `10.0.2.2` instead of `localhost` for Android emulator
   - Ensure `android:usesCleartextTraffic="true"` in AndroidManifest.xml
   - Check that Gateway is running on port 8084

3. **Authentication fails**
   - Verify user exists in MongoDB (wassalni database)
   - Check password matches exactly
   - Ensure authentication-service is registered in Eureka

4. **Gradle sync failed**
   - File → Invalidate Caches / Restart
   - Delete `.gradle` and `.idea` folders
   - Sync again

## Implemented Features

- [x] Authentication (Login/Signup)
- [x] Role-based UI (Passenger vs Driver)
- [x] Ride Search with city/date filters
- [x] Gender filter for rides (client-side filtering)
- [x] Seat selection dialog when booking (1-N seats)
- [x] Ride Publishing with price support (Driver)
- [x] Booking Management (Passenger)
- [x] Pending Booking Requests (Driver)
- [x] Accept/Reject Bookings (Driver)
- [x] Auto-update available seats on booking accept
- [x] My Rides management (Driver)
- [x] Pull-to-refresh for bookings
- [x] Driver identity verification fields
- [x] Hide rides with no available seats

## API Ready (Models implemented)

- [x] Reviews API integration
- [x] Reports API integration
- [x] User ban/unban API
- [x] Average ratings API

## Future Enhancements

- [ ] Implement reviews UI
- [ ] Implement reports UI
- [ ] Add real-time notifications
- [ ] Add maps integration for route visualization
- [ ] Add photo upload for user profiles
- [ ] Implement chat between drivers and passengers
- [ ] Add payment integration

## License

This project is part of the Carpooling Application system.
