# Wssalni - Technical Presentation Guide

## 🏗️ Project Overview

**Wssalni** is a ride-sharing/carpooling application built with a modern microservices architecture. The project consists of three main components:

| Component | Technology | Purpose |
|-----------|------------|---------|
| **CarpoolingApp** | Kotlin/Android | Native mobile frontend |
| **Backend** | Spring Boot (Java) | Microservices API |
| **Frontend** | Angular | Web frontend |

---

## 📱 CarpoolingApp (Kotlin/Android Frontend)

### Architecture Overview

```
CarpoolingApp/
├── app/src/main/java/com/carpooling/app/
│   ├── activities/               # Screen Controllers
│   │   ├── MainActivity.kt       # Splash screen
│   │   ├── LoginActivity.kt      # User authentication
│   │   ├── SignupActivity.kt     # User registration
│   │   ├── DashboardActivity.kt  # Main app (Passenger/Driver)
│   │   └── AdminDashboardActivity.kt  # Admin panel
│   │
│   ├── fragments/                # UI Fragments (Tabs)
│   │   ├── SearchRidesFragment.kt      # Passenger: Find rides
│   │   ├── MyBookingsFragment.kt       # Passenger: View bookings
│   │   ├── MyLastRidesFragment.kt      # Passenger: Ride history
│   │   ├── MyRidesFragment.kt          # Driver: Published rides
│   │   ├── PendingBookingsFragment.kt  # Driver: Accept/Reject requests
│   │   ├── PublishRideFragment.kt      # Driver: Create rides
│   │   ├── NotificationsFragment.kt    # Both: View notifications
│   │   ├── AdminRidesHistoryFragment.kt    # Admin: All rides
│   │   ├── AdminManageReportsFragment.kt   # Admin: Handle reports
│   │   └── AdminManageUsersFragment.kt     # Admin: Ban/Unban users
│   │
│   ├── adapters/                 # RecyclerView Adapters
│   │   ├── RideAdapter.kt            # Display ride cards
│   │   ├── BookingAdapter.kt         # Display bookings
│   │   ├── DriverRideAdapter.kt      # Driver's ride cards
│   │   ├── PendingBookingAdapter.kt  # Pending request cards
│   │   ├── NotificationAdapter.kt    # Notification items
│   │   ├── AdminRideAdapter.kt       # Admin ride view
│   │   ├── AdminReportAdapter.kt     # Admin report cards
│   │   └── AdminUserAdapter.kt       # Admin user management
│   │
│   ├── models/                   # Data Classes
│   │   ├── User.kt               # User model
│   │   ├── Ride.kt               # Ride + City models
│   │   └── LoginRequest.kt       # All DTOs (Request/Response)
│   │
│   ├── api/
│   │   └── ApiService.kt         # Retrofit API interface
│   │
│   ├── network/
│   │   └── RetrofitClient.kt     # HTTP client configuration
│   │
│   └── utils/
│       └── SessionManager.kt     # SharedPreferences session
```

### Key Technologies Used

| Technology | Purpose |
|------------|---------|
| **Kotlin** | Programming language |
| **ViewBinding** | Type-safe view access |
| **Retrofit 2.9.0** | REST API client |
| **OkHttp 4.12.0** | HTTP client with logging |
| **Coroutines** | Async operations |
| **Material Design 3** | UI components |
| **RecyclerView** | Efficient list display |
| **SwipeRefreshLayout** | Pull-to-refresh |

### How Components Work Together

```
┌─────────────────────────────────────────────────────────────────┐
│                        DashboardActivity                         │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ TabLayout (Role-based tabs)                                 │ │
│  │  • Passenger: Search | My Bookings | History                │ │
│  │  • Driver: My Rides | Pending | Publish                     │ │
│  └─────────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ FragmentContainer                                            │ │
│  │  ┌─────────────────────────────────────────────────────┐    │ │
│  │  │ Fragment (e.g., SearchRidesFragment)                │    │ │
│  │  │  • RecyclerView + Adapter                           │    │ │
│  │  │  • API calls via RetrofitClient                     │    │ │
│  │  │  • User session via SessionManager                  │    │ │
│  │  └─────────────────────────────────────────────────────┘    │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### Data Flow Example: Booking a Ride

```
User taps "Book" → RideAdapter → SearchRidesFragment.bookRide()
                                        │
                                        ▼
                          RetrofitClient.apiService.createBooking()
                                        │
                                        ▼ HTTP POST
                     ┌──────────────────────────────────────┐
                     │ Gateway (8084)                        │
                     │ → booking-service/api/bookings/create │
                     └──────────────────────────────────────┘
                                        │
                                        ▼
                     ┌──────────────────────────────────────┐
                     │ BookingService.bookRide()             │
                     │ → Creates PENDING booking in MongoDB  │
                     └──────────────────────────────────────┘
                                        │
                                        ▼
                          Response → UI updates → Toast message
```

---

## 🔧 Backend Microservices Architecture

### Service Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Eureka Server (8083)                            │
│                         Service Discovery & Registry                         │
└─────────────────────────────────────────────────────────────────────────────┘
                                       ▲
                                       │ Register
          ┌────────────────────────────┼────────────────────────────┐
          │                            │                            │
          ▼                            ▼                            ▼
┌─────────────────┐      ┌─────────────────────┐      ┌─────────────────┐
│ Gateway (8084)  │      │ Auth Service (8081) │      │ Ride Svc (8085) │
│ API Gateway     │      │ User Management     │      │ Ride CRUD       │
└─────────────────┘      └─────────────────────┘      └─────────────────┘
          │                            │                            │
          │              ┌─────────────────────┐      ┌─────────────────┐
          │              │ Booking Svc (8082)  │      │ Review (8086)   │
          │              │ Booking Management  │      │ Ratings System  │
          │              └─────────────────────┘      └─────────────────┘
          │                            │
          │              ┌─────────────────────┐
          │              │ Report Svc (8087)   │
          │              │ Safety Reports      │
          │              └─────────────────────┘
          │
          └─────────────── All requests routed through Gateway ──────────────
```

### Service Details

#### 1. Eureka Server (Port 8083)
**Purpose**: Service discovery and registration

**Key Feature**: All microservices register here, enabling dynamic service discovery without hardcoded URLs.

```yaml
# application.yml
spring:
  application:
    name: eureka-server
server:
  port: 8083
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

---

#### 2. Gateway Service (Port 8084)
**Purpose**: Single entry point, request routing

**Features**:
- Routes all API requests to appropriate microservices
- Handles CORS configuration
- Service discovery integration

```yaml
# application.yml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
```

**Routing Pattern**: `/{service-name}/api/*` → Routed to service

---

#### 3. Authentication Service (Port 8081)
**Purpose**: User registration, login, and management

**Endpoints**:
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/auth/createAccount` | User registration |
| POST | `/api/auth/authenticate` | Login |
| GET | `/api/auth/users` | List all users (Admin) |
| GET | `/api/auth/users/{id}` | Get user by ID |
| PUT | `/api/auth/users/{id}/ban` | Ban user (Admin) |
| PUT | `/api/auth/users/{id}/unban` | Unban user (Admin) |

**Key Implementation**:
```java
// AuthenticationService.java
public AppUser createAccount(CreateAccountRequest request) {
    // Check email uniqueness
    if (userRepo.findByEmail(request.email()).isPresent()) {
        throw new RuntimeException("Email already taken");
    }
    
    // Create Driver or Passenger based on userType
    AppUser user;
    if (request.userType() == AppUser.UserType.DRIVER) {
        Driver d = new Driver();
        d.setLicenseNumber(request.licenseNumber());
        d.setVehiclePlate(request.vehiclePlate());
        user = d;
    } else {
        Passenger p = new Passenger();
        user = p;
    }
    
    return userRepo.save(user);
}
```

**Data Models**:
- `AppUser` (base class): id, email, password, phoneNumber, gender, userType, isBanned
- `Driver` extends `AppUser`: licenseNumber, vehicleNumber, vehiclePlate, isVerified, rating
- `Passenger` extends `AppUser`: preferredPaymentMethod, rating

---

#### 4. Ride Service (Port 8085)
**Purpose**: Ride creation, search, and management

**Endpoints**:
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/rides/create` | Publish new ride |
| GET | `/api/rides` | Get all rides |
| GET | `/api/rides/search` | Search with filters |
| GET | `/api/rides/{id}` | Get ride by ID |
| GET | `/api/rides/driver/{driverId}` | Driver's rides |
| DELETE | `/api/rides/{id}` | Delete ride |
| PUT | `/api/rides/{id}/seats` | Update available seats |

**Search Implementation**:
```java
// RideServiceImpl.java
public List<Ride> searchRides(String departureCity, String destinationCity, LocalDate date) {
    return rideRepository.findAll().stream()
        .filter(ride -> {
            boolean matches = true;
            if (departureCity != null) {
                matches = ride.getDepartureCity().getName()
                    .toLowerCase().contains(departureCity.toLowerCase());
            }
            if (destinationCity != null) {
                matches = matches && ride.getDestinationCity().getName()
                    .toLowerCase().contains(destinationCity.toLowerCase());
            }
            if (date != null) {
                matches = matches && ride.getDepartureDate().equals(date);
            }
            return matches && ride.getStatus() == RideStatus.SCHEDULED 
                   && ride.getAvailableSeats() > 0;
        })
        .toList();
}
```

**Data Model**:
```java
class Ride {
    String id;
    City departureCity;      // { name, postalCode }
    City destinationCity;
    LocalDate departureDate;
    Integer availableSeats;
    Double pricePerSeat;
    String driverId;
    RideStatus status;       // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
}
```

---

#### 5. Booking Service (Port 8082)
**Purpose**: Booking management (create, accept, reject, cancel)

**Endpoints**:
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/bookings/create` | Create booking |
| DELETE | `/api/bookings/{id}` | Cancel booking |
| GET | `/api/bookings/passenger/{id}` | Passenger's bookings |
| GET | `/api/bookings/driver/{id}/pending` | Driver's pending requests |
| POST | `/api/bookings/{id}/accept` | Accept booking |
| POST | `/api/bookings/{id}/reject` | Reject booking |

**Key Feature - Inter-Service Communication**:
```java
// BookingServiceImpl.java - When accepting booking
public void acceptBooking(String bookingId, String driverId) {
    Booking booking = bookingRepository.findById(bookingId).orElseThrow();
    booking.setStatus(BookingStatus.ACCEPTED);
    bookingRepository.save(booking);
    
    // Call Ride Service to update available seats
    webClientBuilder.build()
        .put()
        .uri("http://ride-service/api/rides/{rideId}/seats?seatsToDeduct={seats}",
             booking.getRideId(), booking.getSeatsBooked())
        .retrieve()
        .bodyToMono(Object.class)
        .block();
}
```

**Booking Status Flow**:
```
PENDING → ACCEPTED → (COMPLETED)
       └→ REJECTED
       └→ CANCELLED (by passenger)
```

---

#### 6. Review Service (Port 8086)
**Purpose**: Rating system for drivers and passengers

**Endpoints**:
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/reviews/create` | Create review |
| GET | `/api/reviews/user/{id}` | Get user's reviews |
| GET | `/api/reviews/user/{id}/average` | Get average rating |
| GET | `/api/reviews/ride/{id}` | Reviews for a ride |

**Review Types**:
- `DRIVER` - Passenger rates driver
- `PASSENGER` - Driver rates passenger

---

#### 7. Report Service (Port 8087)
**Purpose**: Safety reporting and user moderation

**Endpoints**:
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/reports/create` | Create report |
| GET | `/api/reports` | All reports (Admin) |
| GET | `/api/reports/status/{status}` | Filter by status |
| PUT | `/api/reports/{id}/status` | Update status (Admin) |

**Report Reasons**:
- `INAPPROPRIATE_BEHAVIOR`
- `NO_SHOW`
- `UNSAFE_DRIVING`
- `OTHER`

**Report Status Flow**:
```
PENDING → REVIEWED → RESOLVED (user banned)
                  └→ DISMISSED (false report)
```

---

## 🗓️ Sprint Feature Mapping

### Sprint 1: Core Access & Discovery ✅

| Feature | Backend Service | Android Component | Status |
|---------|-----------------|-------------------|--------|
| User Signup | auth-service `/createAccount` | `SignupActivity.kt` | ✅ Implemented |
| User Login | auth-service `/authenticate` | `LoginActivity.kt` | ✅ Implemented |
| Search Rides | ride-service `/search` | `SearchRidesFragment.kt` | ✅ Implemented |
| Filter by Gender | Client-side filtering | `SearchRidesFragment.kt` | ✅ Implemented |
| Publish Ride | ride-service `/create` | `PublishRideFragment.kt` | ✅ Implemented |
| Driver Verification | auth-service (Driver fields) | `SignupActivity.kt` | ✅ Implemented |

### Sprint 2: Booking & Reviews ✅

| Feature | Backend Service | Android Component | Status |
|---------|-----------------|-------------------|--------|
| Book Ride | booking-service `/create` | `SearchRidesFragment.kt` | ✅ Implemented |
| View Bookings | booking-service `/passenger/{id}` | `MyBookingsFragment.kt` | ✅ Implemented |
| Cancel Booking | booking-service `DELETE` | `BookingAdapter.kt` | ✅ Implemented |
| Accept Booking | booking-service `/accept` | `PendingBookingsFragment.kt` | ✅ Implemented |
| Reject Booking | booking-service `/reject` | `PendingBookingsFragment.kt` | ✅ Implemented |
| View Reviews | review-service `/user/{id}` | API Ready | ⚠️ API Ready |
| Notifications | Client-side (polling) | `NotificationsFragment.kt` | ✅ Implemented |

### Sprint 3: Administration & Safety ✅

| Feature | Backend Service | Android Component | Status |
|---------|-----------------|-------------------|--------|
| View Rides History | ride-service `/rides` | `AdminRidesHistoryFragment.kt` | ✅ Implemented |
| Manage Reports | report-service `/reports` | `AdminManageReportsFragment.kt` | ✅ Implemented |
| Ban User | auth-service `/ban` | `AdminManageReportsFragment.kt` | ✅ Implemented |
| Unban User | auth-service `/unban` | `AdminManageUsersFragment.kt` | ✅ Implemented |
| Report Problem | report-service `/create` | API Ready | ⚠️ API Ready |
| Rate Driver | review-service `/create` | API Ready | ⚠️ API Ready |
| Rate Passenger | review-service `/create` | API Ready | ⚠️ API Ready |

---

## 🔐 Authentication Flow

```
┌──────────────────────────────────────────────────────────────────────┐
│                         LOGIN FLOW                                    │
├──────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  1. LoginActivity                                                     │
│     │                                                                 │
│     └─► RetrofitClient.apiService.login(LoginRequest)                │
│         │                                                             │
│         ▼                                                             │
│     POST /authentication-service/api/auth/authenticate               │
│         │                                                             │
│         ▼                                                             │
│     AuthResponse { user, token, status }                             │
│         │                                                             │
│         ├─► SessionManager.saveUser(user, token)                     │
│         │   (Saves to SharedPreferences)                             │
│         │                                                             │
│         ├─► RetrofitClient.setAuthToken(token)                       │
│         │   (Sets Bearer token for future requests)                  │
│         │                                                             │
│         └─► Navigate based on role:                                  │
│             • ADMIN → AdminDashboardActivity                         │
│             • DRIVER/PASSENGER → DashboardActivity                   │
│                                                                       │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Database (MongoDB)

Each microservice has its own MongoDB collection:

| Service | Database | Collections |
|---------|----------|-------------|
| Authentication | wassalni | users (AppUser, Driver, Passenger) |
| Ride | wassalni | rides |
| Booking | wassalni | bookings |
| Review | wassalni | reviews |
| Report | wassalni | reports |

---

## 🌐 API Communication Pattern

### Request Flow

```
┌─────────┐     ┌─────────┐     ┌─────────┐     ┌─────────────┐
│ Android │────►│ Gateway │────►│ Eureka  │────►│ Microservice│
│   App   │     │  8084   │     │  8083   │     │   808X      │
└─────────┘     └─────────┘     └─────────┘     └─────────────┘
     │               │               │                │
     │   HTTP POST   │   Lookup      │   Forward      │
     │──────────────►│──────────────►│───────────────►│
     │               │               │                │
     │               │◄──────────────│◄───────────────│
     │◄──────────────│   Response    │   Response     │
     │   JSON        │               │                │
```

### RetrofitClient Configuration

```kotlin
object RetrofitClient {
    // Gateway URL (routes to all services)
    private const val BASE_URL = "http://10.0.2.2:8084/"
    
    // Auth token storage
    private val authToken = AtomicReference<String?>(null)
    
    // OkHttp with interceptors
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)  // Debug logging
        .addInterceptor { chain ->           // Auth header
            val request = chain.request().newBuilder()
                .header("Authorization", "Bearer ${authToken.get()}")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
}
```

---

## 🎨 UI/UX Architecture

### Role-Based UI

```
┌────────────────────────────────────────────────────────────────┐
│                     DashboardActivity                           │
│                                                                 │
│  if (userRole == "DRIVER"):                                    │
│  ┌────────────┬────────────────┬────────────────┐              │
│  │ My Rides   │ Pending Req.   │ Publish Ride   │              │
│  │            │ (Accept/Reject)│                │              │
│  └────────────┴────────────────┴────────────────┘              │
│                                                                 │
│  if (userRole == "PASSENGER"):                                 │
│  ┌────────────┬────────────────┬────────────────┐              │
│  │ Search     │ My Bookings    │ Ride History   │              │
│  │ Rides      │                │                │              │
│  └────────────┴────────────────┴────────────────┘              │
│                                                                 │
│  if (userRole == "ADMIN"):                                     │
│  ┌────────────┬────────────────┬────────────────┐              │
│  │ All Rides  │ Manage Reports │ Manage Users   │              │
│  │            │                │ (Ban/Unban)    │              │
│  └────────────┴────────────────┴────────────────┘              │
└────────────────────────────────────────────────────────────────┘
```

### Notification System (Client-Side)

```kotlin
// NotificationsFragment.kt - No separate notification service needed!
private fun loadNotifications() {
    if (isDriver) {
        // For drivers: pending bookings = notifications
        val bookings = apiService.getPendingBookingsForDriver(userId)
        // Display as "New booking request" notifications
    } else {
        // For passengers: ACCEPTED/REJECTED bookings = notifications
        val bookings = apiService.getPassengerBookings(userId)
            .filter { it.status in listOf("ACCEPTED", "REJECTED") }
        // Display as "Your booking was accepted/rejected" notifications
    }
}
```

---

## 🚀 How to Run

### Backend Services (Start in Order)

```bash
# 1. Start Eureka Server (Service Discovery)
cd "backend/eureka-server(8083)"
mvn spring-boot:run

# 2. Start Gateway (API Router)
cd "backend/gateway-service(8084)"
mvn spring-boot:run

# 3. Start Microservices (any order)
cd "backend/authentication-service(8081)"
mvn spring-boot:run

cd "backend/ride-service(8085)"
mvn spring-boot:run

cd "backend/booking-service(8082)"
mvn spring-boot:run

cd "backend/review-service(8086)"
mvn spring-boot:run

cd "backend/report-service(8087)"
mvn spring-boot:run
```

### Verify Services
- Eureka Dashboard: http://localhost:8083
- All services should be registered

### Android App
1. Open `CarpoolingApp` in Android Studio
2. Configure `RetrofitClient.kt` BASE_URL:
   - Emulator: `http://10.0.2.2:8084/`
   - Physical device: `http://<YOUR_IP>:8084/`
     - Windows: Run `ipconfig` and use your IPv4 address
     - macOS/Linux: Run `ifconfig` or `ip addr` and use your local IP
3. Run on device/emulator

---

## 📝 Key Technical Decisions

1. **Microservices Architecture**: Each domain (Auth, Rides, Bookings, Reviews, Reports) has its own service for scalability and maintainability.

2. **Gateway Pattern**: Single entry point handles routing, making it easy to add authentication/authorization in one place.

3. **Client-Side Notifications**: Instead of a separate notification service with WebSockets, the app polls existing endpoints, reducing complexity.

4. **Gender Filter (Client-Side)**: The filter fetches driver info and filters locally, avoiding complex backend queries.

5. **ViewBinding**: Type-safe view access without the overhead of Butterknife or Kotlin synthetics.

6. **Coroutines**: Modern async handling with lifecycle awareness.

---

## 🔒 Security Considerations

| Area | Current State | Recommendation |
|------|---------------|----------------|
| Password Storage | Basic implementation | Use BCrypt hashing |
| Auth Token | Simple token pattern | Implement JWT with expiration |
| API Security | Bearer token in headers | Add role-based authorization |
| User Banning | Server-side check on login | ✅ Working correctly |

---

## 📈 Future Enhancements

- [ ] Real JWT authentication
- [ ] Password encryption (BCrypt)
- [ ] WebSocket for real-time notifications
- [ ] Google Maps integration
- [ ] Payment gateway
- [ ] Chat feature
- [ ] Push notifications (FCM)
- [ ] Profile photos
- [ ] Unit tests

---

## 🎯 Presentation Tips

1. **Start with Architecture**: Show the microservices diagram
2. **Demo Login Flow**: Show how token is saved and used
3. **Demo Booking Flow**: Show inter-service communication
4. **Highlight Role-Based UI**: Show different tabs for different users
5. **Show Eureka Dashboard**: Services registered
6. **Mention Sprint Completion**: All 3 sprints implemented
