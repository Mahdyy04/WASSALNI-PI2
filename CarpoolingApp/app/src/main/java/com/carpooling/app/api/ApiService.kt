package com.carpooling.app.api

import com.carpooling.app.models.AuthResponse
import com.carpooling.app.models.AverageRatingResponse
import com.carpooling.app.models.Booking
import com.carpooling.app.models.BookingResponse
import com.carpooling.app.models.CreateBookingRequest
import com.carpooling.app.models.CreateReportRequest
import com.carpooling.app.models.CreateReviewRequest
import com.carpooling.app.models.CreateRideRequest
import com.carpooling.app.models.LoginRequest
import com.carpooling.app.models.Notification
import com.carpooling.app.models.NotificationCountResponse
import com.carpooling.app.models.Report
import com.carpooling.app.models.Review
import com.carpooling.app.models.Ride
import com.carpooling.app.models.SignupRequest
import com.carpooling.app.models.User
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service interface for the Wassalni carpooling app.
 * 
 * Routes through Spring Cloud Gateway with Eureka discovery:
 * - Gateway: http://10.0.2.2:8084 (Android emulator)
 * - Pattern: /{service-name}/api/{endpoint}
 * 
 * Services:
 * - authentication-service (port 8081): /authentication-service/api/auth/*
 * - ride-service (port 8085): /ride-service/api/rides/*
 * - booking-service (port 8082): /booking-service/api/bookings/*
 * - review-service (port 8086): /review-service/api/reviews/*
 * - report-service (port 8087): /report-service/api/reports/*
 */
interface ApiService {
    
    // ==================== Authentication ====================
    // Service: authentication-service (8081)
    
    @POST("authentication-service/api/auth/authenticate")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("authentication-service/api/auth/createAccount")
    suspend fun signup(@Body request: SignupRequest): Response<User>
    
    @GET("authentication-service/api/auth/users")
    suspend fun getAllUsers(): Response<List<User>>
    
    @GET("authentication-service/api/auth/users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): Response<User>
    
    @GET("authentication-service/api/auth/users/email/{email}")
    suspend fun getUserByEmail(@Path("email") email: String): Response<User>
    
    @PUT("authentication-service/api/auth/users/{userId}/ban")
    suspend fun banUser(@Path("userId") userId: String): Response<User>
    
    @PUT("authentication-service/api/auth/users/{userId}/unban")
    suspend fun unbanUser(@Path("userId") userId: String): Response<User>
    
    // ==================== Rides ====================
    // Service: ride-service (8085)
    
    @GET("ride-service/api/rides")
    suspend fun getAllRides(): Response<List<Ride>>
    
    @GET("ride-service/api/rides/search")
    suspend fun searchRides(
        @Query("departureCity") departureCity: String?,
        @Query("destinationCity") destinationCity: String?,
        @Query("date") date: String?
    ): Response<List<Ride>>
    
    @GET("ride-service/api/rides/{rideId}")
    suspend fun getRideById(@Path("rideId") rideId: String): Response<Ride>
    
    @GET("ride-service/api/rides/driver/{driverId}")
    suspend fun getDriverRides(@Path("driverId") driverId: String): Response<List<Ride>>
    
    @POST("ride-service/api/rides/create")
    suspend fun publishRide(@Body request: CreateRideRequest): Response<Ride>
    
    @DELETE("ride-service/api/rides/{rideId}")
    suspend fun deleteRide(
        @Path("rideId") rideId: String,
        @Query("driverId") driverId: String
    ): Response<Unit>
    
    // ==================== Bookings ====================
    // Service: booking-service (8082)
    
    @POST("booking-service/api/bookings/create")
    suspend fun createBooking(@Body request: CreateBookingRequest): Response<BookingResponse>
    
    @GET("booking-service/api/bookings/passenger/{passengerId}")
    suspend fun getPassengerBookings(@Path("passengerId") passengerId: String): Response<List<Booking>>
    
    @GET("booking-service/api/bookings/ride/{rideId}")
    suspend fun getRideBookings(@Path("rideId") rideId: String): Response<List<Booking>>
    
    @GET("booking-service/api/bookings/driver/{driverId}/pending")
    suspend fun getPendingBookingsForDriver(@Path("driverId") driverId: String): Response<List<Booking>>
    
    @DELETE("booking-service/api/bookings/{bookingId}")
    suspend fun cancelBooking(
        @Path("bookingId") bookingId: String,
        @Query("passengerId") passengerId: String
    ): Response<Unit>
    
    @POST("booking-service/api/bookings/{bookingId}/accept")
    suspend fun acceptBooking(
        @Path("bookingId") bookingId: String,
        @Query("driverId") driverId: String
    ): Response<Unit>
    
    @POST("booking-service/api/bookings/{bookingId}/reject")
    suspend fun rejectBooking(
        @Path("bookingId") bookingId: String,
        @Query("driverId") driverId: String
    ): Response<Unit>
    
    // ==================== Reviews ====================
    // Service: review-service (8086)
    
    @POST("review-service/api/reviews/create")
    suspend fun createReview(@Body request: CreateReviewRequest): Response<Review>
    
    @GET("review-service/api/reviews/user/{userId}")
    suspend fun getReviewsByUser(@Path("userId") userId: String): Response<List<Review>>
    
    @GET("review-service/api/reviews/user/{userId}/average")
    suspend fun getAverageRating(@Path("userId") userId: String): Response<AverageRatingResponse>
    
    @GET("review-service/api/reviews/ride/{rideId}")
    suspend fun getReviewsByRide(@Path("rideId") rideId: String): Response<List<Review>>
    
    @GET("review-service/api/reviews/user/{userId}/type/{type}")
    suspend fun getReviewsByUserAndType(
        @Path("userId") userId: String,
        @Path("type") type: String
    ): Response<List<Review>>
    
    // ==================== Reports ====================
    // Service: report-service (8087)
    
    @POST("report-service/api/reports/create")
    suspend fun createReport(@Body request: CreateReportRequest): Response<Report>
    
    @GET("report-service/api/reports")
    suspend fun getAllReports(): Response<List<Report>>
    
    @GET("report-service/api/reports/status/{status}")
    suspend fun getReportsByStatus(@Path("status") status: String): Response<List<Report>>
    
    @GET("report-service/api/reports/reporter/{reporterId}")
    suspend fun getReportsByReporter(@Path("reporterId") reporterId: String): Response<List<Report>>
    
    @GET("report-service/api/reports/reported/{reportedUserId}")
    suspend fun getReportsByReportedUser(@Path("reportedUserId") reportedUserId: String): Response<List<Report>>
    
    @GET("report-service/api/reports/{reportId}")
    suspend fun getReportById(@Path("reportId") reportId: String): Response<Report>
    
    @PUT("report-service/api/reports/{reportId}/status")
    suspend fun updateReportStatus(
        @Path("reportId") reportId: String,
        @Query("status") status: String
    ): Response<Report>
    
    // ==================== Notifications ====================
    // Service: notification-service (8088)
    
    @GET("notification-service/api/notifications/user/{userId}")
    suspend fun getNotifications(@Path("userId") userId: String): Response<List<Notification>>
    
    @GET("notification-service/api/notifications/user/{userId}/unread")
    suspend fun getUnreadNotifications(@Path("userId") userId: String): Response<List<Notification>>
    
    @GET("notification-service/api/notifications/user/{userId}/count")
    suspend fun getUnreadNotificationCount(@Path("userId") userId: String): Response<NotificationCountResponse>
    
    @PUT("notification-service/api/notifications/{notificationId}/read")
    suspend fun markNotificationAsRead(@Path("notificationId") notificationId: String): Response<Unit>
    
    @PUT("notification-service/api/notifications/user/{userId}/read-all")
    suspend fun markAllNotificationsAsRead(@Path("userId") userId: String): Response<Unit>
}
