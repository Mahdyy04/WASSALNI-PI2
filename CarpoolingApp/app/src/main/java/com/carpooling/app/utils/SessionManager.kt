package com.carpooling.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.carpooling.app.models.User

class SessionManager(context: Context) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("CarpoolingAppPrefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_TOKEN = "user_token"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
    
    fun saveUser(user: User) {
        prefs.edit().apply {
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_NAME, user.name)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_ROLE, user.role)
            putString(KEY_USER_TOKEN, user.token)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    fun getUser(): User {
        return User(
            id = prefs.getString(KEY_USER_ID, "") ?: "",
            name = prefs.getString(KEY_USER_NAME, "") ?: "",
            email = prefs.getString(KEY_USER_EMAIL, "") ?: "",
            role = prefs.getString(KEY_USER_ROLE, "") ?: "",
            token = prefs.getString(KEY_USER_TOKEN, "") ?: ""
        )
    }
    
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    fun logout() {
        prefs.edit().clear().apply()
    }
}
