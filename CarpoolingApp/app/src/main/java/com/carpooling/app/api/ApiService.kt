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
import com.carpooling.app.models.Report
import com.carpooling.app.models.Review
import com.carpooling.app.models.Ride
import com.carpooling.app.models.SignupRequest
import com.carpooling.app.models.User
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // ==================== Authentication ====================
    
    @POST("api/auth/authenticate")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("api/auth/createAccount")
    suspend fun signup(@Body request: SignupRequest): Response<User>
    
    @GET("api/auth/users")
    suspend fun getAllUsers(): Response<List<User>>
    
    @GET("api/auth/users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): Response<User>
    
    @GET("api/auth/users/email/{email}")
    suspend fun getUserByEmail(@Path("email") email: String): Response<User>
    
    @PUT("api/auth/users/{userId}/ban")
    suspend fun banUser(@Path("userId") userId: String): Response<User>
    
    @PUT("api/auth/users/{userId}/unban")
    suspend fun unbanUser(@Path("userId") userId: String): Response<User>
    
    // ==================== Rides ====================
    
    @GET("api/rides")
    suspend fun getAllRides(): Response<List<Ride>>
    
    @GET("api/rides/search")
    suspend fun searchRides(
        @Query("departureCity") departureCity: String?,
        @Query("destinationCity") destinationCity: String?,
        @Query("date") date: String?
    ): Response<List<Ride>>
    
    @GET("api/rides/{rideId}")
    suspend fun getRideById(@Path("rideId") rideId: String): Response<Ride>
    
    @GET("api/rides/driver/{driverId}")
    suspend fun getDriverRides(@Path("driverId") driverId: String): Response<List<Ride>>
    
    @POST("api/rides/create")
    suspend fun publishRide(@Body request: CreateRideRequest): Response<Ride>
    
    @DELETE("api/rides/{rideId}")
    suspend fun deleteRide(
        @Path("rideId") rideId: String,
        @Query("driverId") driverId: String
    ): Response<String>
    
    // ==================== Bookings ====================
    
    @POST("api/bookings/create")
    suspend fun createBooking(@Body request: CreateBookingRequest): Response<BookingResponse>
    
    @GET("api/bookings/passenger/{passengerId}")
    suspend fun getPassengerBookings(@Path("passengerId") passengerId: String): Response<List<Booking>>
    
    @GET("api/bookings/ride/{rideId}")
    suspend fun getRideBookings(@Path("rideId") rideId: String): Response<List<Booking>>
    
    @GET("api/bookings/driver/{driverId}/pending")
    suspend fun getPendingBookingsForDriver(@Path("driverId") driverId: String): Response<List<Booking>>
    
    @DELETE("api/bookings/{bookingId}")
    suspend fun cancelBooking(
        @Path("bookingId") bookingId: String,
        @Query("passengerId") passengerId: String
    ): Response<String>
    
    @POST("api/bookings/{bookingId}/accept")
    suspend fun acceptBooking(
        @Path("bookingId") bookingId: String,
        @Query("driverId") driverId: String
    ): Response<String>
    
    @POST("api/bookings/{bookingId}/reject")
    suspend fun rejectBooking(
        @Path("bookingId") bookingId: String,
        @Query("driverId") driverId: String
    ): Response<String>
    
    // ==================== Reviews ====================
    
    @POST("api/reviews/create")
    suspend fun createReview(@Body request: CreateReviewRequest): Response<Review>
    
    @GET("api/reviews/user/{userId}")
    suspend fun getReviewsByUser(@Path("userId") userId: String): Response<List<Review>>
    
    @GET("api/reviews/user/{userId}/average")
    suspend fun getAverageRating(@Path("userId") userId: String): Response<AverageRatingResponse>
    
    @GET("api/reviews/ride/{rideId}")
    suspend fun getReviewsByRide(@Path("rideId") rideId: String): Response<List<Review>>
    
    @GET("api/reviews/user/{userId}/type/{type}")
    suspend fun getReviewsByUserAndType(
        @Path("userId") userId: String,
        @Path("type") type: String
    ): Response<List<Review>>
    
    // ==================== Reports ====================
    
    @POST("api/reports/create")
    suspend fun createReport(@Body request: CreateReportRequest): Response<Report>
    
    @GET("api/reports")
    suspend fun getAllReports(): Response<List<Report>>
    
    @GET("api/reports/status/{status}")
    suspend fun getReportsByStatus(@Path("status") status: String): Response<List<Report>>
    
    @GET("api/reports/reporter/{reporterId}")
    suspend fun getReportsByReporter(@Path("reporterId") reporterId: String): Response<List<Report>>
    
    @GET("api/reports/reported/{reportedUserId}")
    suspend fun getReportsByReportedUser(@Path("reportedUserId") reportedUserId: String): Response<List<Report>>
    
    @GET("api/reports/{reportId}")
    suspend fun getReportById(@Path("reportId") reportId: String): Response<Report>
    
    @PUT("api/reports/{reportId}/status")
    suspend fun updateReportStatus(
        @Path("reportId") reportId: String,
        @Query("status") status: String
    ): Response<Report>
}
