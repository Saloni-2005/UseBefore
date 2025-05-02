package com.example.usebefore.repository

import android.content.Context
import android.content.SharedPreferences

class UserPreferenceManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)

    fun getCurrentUser(): String? {
        return sharedPreferences.getString("username", null)
    }

    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    fun saveUserPreference(key: String, value: Any) {
        val username = getCurrentUser() ?: return
        val userSpecificKey = "${username}_$key"

        val editor = sharedPreferences.edit()
        when (value) {
            is String -> editor.putString(userSpecificKey, value)
            is Boolean -> editor.putBoolean(userSpecificKey, value)
            is Int -> editor.putInt(userSpecificKey, value)
            is Float -> editor.putFloat(userSpecificKey, value)
            is Long -> editor.putLong(userSpecificKey, value)
            else -> throw IllegalArgumentException("Unsupported preference type")
        }
        editor.apply()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getUserPreference(key: String, defaultValue: T): T {
        val username = getCurrentUser() ?: return defaultValue
        val userSpecificKey = "${username}_$key"

        return when (defaultValue) {
            is String -> sharedPreferences.getString(userSpecificKey, defaultValue) as T
            is Boolean -> sharedPreferences.getBoolean(userSpecificKey, defaultValue) as T
            is Int -> sharedPreferences.getInt(userSpecificKey, defaultValue) as T
            is Float -> sharedPreferences.getFloat(userSpecificKey, defaultValue) as T
            is Long -> sharedPreferences.getLong(userSpecificKey, defaultValue) as T
            else -> defaultValue
        }
    }

    fun clearUserPreferences(username: String) {
        val editor = sharedPreferences.edit()
        val allPrefs = sharedPreferences.all

        // Remove all preferences that start with the username
        for (key in allPrefs.keys) {
            if (key.startsWith("${username}_")) {
                editor.remove(key)
            }
        }

        editor.apply()
    }
}