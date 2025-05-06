package com.example.usebefore.viewmodel

import android.content.Context
import android.content.SharedPreferences

class PreferenceHelper(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "UseBefore_Prefs"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_NOTIFICATION_TIME_HOUR = "notification_time_hour"
        private const val KEY_NOTIFICATION_TIME_MINUTE = "notification_time_minute"
        private const val KEY_ALERT_THRESHOLD_DAYS = "alert_threshold_days"

        // Default values
        private const val DEFAULT_NOTIFICATIONS_ENABLED = false
        private const val DEFAULT_NOTIFICATION_TIME_HOUR = 10 // 10:00 AM
        private const val DEFAULT_NOTIFICATION_TIME_MINUTE = 0
        private const val DEFAULT_ALERT_THRESHOLD_DAYS = 5
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, DEFAULT_NOTIFICATIONS_ENABLED)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun getNotificationTimeHour(): Int {
        return sharedPreferences.getInt(KEY_NOTIFICATION_TIME_HOUR, DEFAULT_NOTIFICATION_TIME_HOUR)
    }

    fun getNotificationTimeMinute(): Int {
        return sharedPreferences.getInt(KEY_NOTIFICATION_TIME_MINUTE, DEFAULT_NOTIFICATION_TIME_MINUTE)
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        sharedPreferences.edit()
            .putInt(KEY_NOTIFICATION_TIME_HOUR, hour)
            .putInt(KEY_NOTIFICATION_TIME_MINUTE, minute)
            .apply()
    }

    fun getAlertThresholdDays(): Int {
        return sharedPreferences.getInt(KEY_ALERT_THRESHOLD_DAYS, DEFAULT_ALERT_THRESHOLD_DAYS)
    }

    fun setAlertThresholdDays(days: Int) {
        sharedPreferences.edit().putInt(KEY_ALERT_THRESHOLD_DAYS, days).apply()
    }
}