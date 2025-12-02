package com.carpooling.app.network

import com.carpooling.app.api.ApiService
import com.carpooling.app.api.ModerationApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

object RetrofitClient {

    // Base URL for the gateway service
    // For Android Emulator use: "http://10.0.2.2:8084/"
    // For Physical Device use: "http://<YOUR_COMPUTER_IP>:8084/"
    private const val BASE_URL = "http://10.0.2.2:8084/"

    // Thread-safe storage for auth token
    private val authToken = AtomicReference<String?>(null)

    fun setAuthToken(token: String?) {
        authToken.set(token)
    }

    fun getAuthToken(): String? = authToken.get()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // OkHttpClient commun
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val token = authToken.get()

            val newRequest = if (!token.isNullOrEmpty()) {
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

    // Retrofit commun pour tous les services via la gateway
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Services API
    val apiService: ApiService = retrofit.create(ApiService::class.java)
    val moderationApi: ModerationApi = retrofit.create(ModerationApi::class.java)
}
