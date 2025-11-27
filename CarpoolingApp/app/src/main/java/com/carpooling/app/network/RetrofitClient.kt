package com.carpooling.app.network

import com.carpooling.app.api.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    // Base URL for the gateway service
    // For Android Emulator use: "http://10.0.2.2:8084/"
    // For Physical Device use: "http://<YOUR_COMPUTER_IP>:8084/"
    private const val BASE_URL = "http://10.0.2.2:8084/"
    
    // Variable to store auth token (set from SessionManager when user logs in)
    @Volatile
    private var authToken: String? = null
    
    fun setAuthToken(token: String?) {
        authToken = token
    }
    
    fun getAuthToken(): String? = authToken
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val token = authToken
            
            val newRequest = if (token != null && token.isNotEmpty()) {
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                originalRequest
            }
            
            chain.proceed(newRequest)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
