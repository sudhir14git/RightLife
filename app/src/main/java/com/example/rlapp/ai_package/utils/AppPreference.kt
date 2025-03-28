package com.example.rlapp.ai_package.utils

import android.content.Context
import android.content.SharedPreferences

class AppPreference(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USERNAME = "username"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USERID = "userId"
        // Add more keys for other data you want to save
    }

    fun saveUsername(username: String) {
        sharedPreferences.edit().putString(KEY_USERNAME, username).apply()
    }

    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }
    fun setIsLoggedIn(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false) // Default value is false
    }

    fun clearPreferences() {
        sharedPreferences.edit().clear().apply()
    }

    fun saveUserId(userId: String) {
        sharedPreferences.edit().putString(KEY_USERID, userId).apply()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString(KEY_USERID, null)
    }
}