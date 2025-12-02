package com.carpooling.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.carpooling.app.models.User

class SessionManager(context: Context) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("CarpoolingAppPrefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_GENDER = "user_gender"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_TOKEN = "user_token"
        private const val KEY_USER_IS_BANNED = "user_is_banned"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
    
    fun saveUser(user: User, token: String = "") {
        prefs.edit().apply {
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_PHONE, user.phoneNumber)
            putString(KEY_USER_GENDER, user.gender)
            putString(KEY_USER_ROLE, user.role)
            putString(KEY_USER_TOKEN, token.ifEmpty { user.token })
            putBoolean(KEY_USER_IS_BANNED, user.isBanned)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    fun getUser(): User {
        return User(
            id = prefs.getString(KEY_USER_ID, "") ?: "",
            email = prefs.getString(KEY_USER_EMAIL, "") ?: "",
            phoneNumber = prefs.getString(KEY_USER_PHONE, "") ?: "",
            gender = prefs.getString(KEY_USER_GENDER, "") ?: "",
            role = prefs.getString(KEY_USER_ROLE, "") ?: "",
            token = prefs.getString(KEY_USER_TOKEN, "") ?: "",
            isBanned = prefs.getBoolean(KEY_USER_IS_BANNED, false)
        )
    }
    
    fun getToken(): String {
        return prefs.getString(KEY_USER_TOKEN, "") ?: ""
    }
    
    fun getUserId(): String {
        return prefs.getString(KEY_USER_ID, "") ?: ""
    }
    
    fun getUserRole(): String {
        return prefs.getString(KEY_USER_ROLE, "") ?: ""
    }
    
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    fun isDriver(): Boolean {
        return getUserRole() == "DRIVER"
    }
    
    fun logout() {
        prefs.edit().clear().apply()
    }
}
