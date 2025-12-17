package com.example.sbikemap.utils

import android.content.Context

class TokenManager(context: Context) {
    // Tên file SharedPreferences và Key
    private val PREFS_NAME = "auth_prefs"
    private val ACCESS_TOKEN_KEY = "access_token"
    private val USER_EMAIL_KEY = "user_email"
    private val USER_NAME_KEY = "user_name"

    // Khởi tạo SharedPreferences
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveAuthData(token: String, email: String, name: String) {
        prefs.edit()
            .putString(ACCESS_TOKEN_KEY, token)
            .putString(USER_EMAIL_KEY, email)
            .putString(USER_NAME_KEY, name)
            .apply()
    }

    fun getAccessToken(): String? {
        return prefs.getString(ACCESS_TOKEN_KEY, null)
    }

    fun clearAccessToken() {
        prefs.edit().remove(ACCESS_TOKEN_KEY).apply()
    }

    fun getEmail(): String? {
        return prefs.getString(USER_EMAIL_KEY, null)
    }

    fun getName(): String? {
        return prefs.getString(USER_NAME_KEY, "Người dùng")
    }

    fun saveUserName(name: String) {
        prefs.edit().putString(USER_NAME_KEY, name).apply()
    }

    fun clearAuthData() {
        prefs.edit()
            .remove(ACCESS_TOKEN_KEY)
            .remove(USER_EMAIL_KEY)
            .remove(USER_NAME_KEY)
            .apply()
    }
}