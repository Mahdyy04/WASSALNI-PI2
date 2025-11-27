package com.carpooling.app.api

import com.carpooling.app.models.AuthResponse
import com.carpooling.app.models.Booking
import com.carpooling.app.models.CreateBookingRequest
import com.carpooling.app.models.CreateRideRequest
import com.carpooling.app.models.LoginRequest
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
    
    @GET("api/auth/users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): Response<User>
    
    @GET("api/auth/users/email/{email}")
    suspend fun getUserByEmail(@Path("email") email: String): Response<User>
    
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
    suspend fun createBooking(@Body request: CreateBookingRequest): Response<Booking>
    
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
}
