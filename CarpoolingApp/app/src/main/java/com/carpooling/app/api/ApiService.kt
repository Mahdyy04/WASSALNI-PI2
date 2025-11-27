package com.carpooling.app.api

import com.carpooling.app.models.LoginRequest
import com.carpooling.app.models.Ride
import com.carpooling.app.models.SignupRequest
import com.carpooling.app.models.User
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<User>
    
    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<User>
    
    @GET("rides/search")
    suspend fun searchRides(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("date") date: String
    ): Response<List<Ride>>
    
    @POST("rides")
    suspend fun publishRide(@Body ride: Ride): Response<Ride>
    
    @GET("bookings")
    suspend fun getMyBookings(): Response<List<Ride>>
    
    @POST("bookings/{rideId}")
    suspend fun bookRide(@Path("rideId") rideId: String): Response<Unit>
}
