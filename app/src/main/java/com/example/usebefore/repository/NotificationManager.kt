package com.example.usebefore.repository

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.usebefore.R
import com.example.usebefore.repository.NotificationReceiver
import com.example.usebefore.viewmodel.PreferenceHelper
import com.example.usebefore.view.ExpiryAlertsActivity
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "expiry_notification_channel"
        const val CHANNEL_NAME = "Expiry Notifications"
        const val CHANNEL_DESCRIPTION = "Notifications for items about to expire"

        const val NOTIFICATION_ID_PREFIX = 1000
        const val EXPIRY_NOTIFICATION_REQUEST_CODE = 100
        const val DAILY_NOTIFICATION_REQUEST_CODE = 101
    }

    private val alarmManager by lazy { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    private val notificationManager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager }
    private val preferenceHelper by lazy { PreferenceHelper(context) }

    init {
        createNotificationChannel()
    }

    public fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleNotificationsForItem(item: InventoryItem) {
        if (!preferenceHelper.isNotificationsEnabled()) {
            return
        }

        val alertThresholdDays = preferenceHelper.getAlertThresholdDays()

        val currentTime = System.currentTimeMillis()
        val expiryTime = item.expiryTimestamp
        val timeUntilExpiry = expiryTime - currentTime
        val daysUntilExpiry = TimeUnit.MILLISECONDS.toDays(timeUntilExpiry)

        scheduleDailyNotifications(item, alertThresholdDays)
        scheduleExpiryDateNotification(item)
    }

    fun scheduleDailyNotifications(item: InventoryItem, alertThresholdDays: Int) {
        val expiryTime = item.expiryTimestamp
        val currentTime = System.currentTimeMillis()
        val timeUntilExpiry = expiryTime - currentTime
        val daysUntilExpiry = TimeUnit.MILLISECONDS.toDays(timeUntilExpiry)

        // Only schedule if the item is not yet expired
        if (daysUntilExpiry >= 0) {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = "com.example.usebefore.DAILY_NOTIFICATION"
                putExtra("NOTIFICATION_TYPE", "DAILY")
                putExtra("ITEM_ID", item.id)
                putExtra("ITEM_NAME", item.name)
                putExtra("EXPIRY_DATE", item.expiryTimestamp)
            }

            val notificationTimeHour = preferenceHelper.getNotificationTimeHour()
            val notificationTimeMinute = preferenceHelper.getNotificationTimeMinute()

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, notificationTimeHour)
                set(Calendar.MINUTE, notificationTimeMinute)
                set(Calendar.SECOND, 0)

                // If today's notification time has already passed, schedule for tomorrow
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                DAILY_NOTIFICATION_REQUEST_CODE + item.id.hashCode(),
                intent,
                flags
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // For newer APIs, we set exact alarm and let the receiver reschedule for tomorrow
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
        }
    }

    public fun scheduleExpiryDateNotification(item: InventoryItem) {
        if (item.expiryTimestamp > System.currentTimeMillis()) {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = "com.example.usebefore.EXPIRY_NOTIFICATION"
                putExtra("NOTIFICATION_TYPE", "EXPIRED")
                putExtra("ITEM_ID", item.id)
                putExtra("ITEM_NAME", item.name)
                putExtra("EXPIRY_DATE", item.expiryTimestamp)
            }

            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                EXPIRY_NOTIFICATION_REQUEST_CODE + item.id.hashCode(),
                intent,
                flags
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    item.expiryTimestamp,
                    pendingIntent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    item.expiryTimestamp,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    item.expiryTimestamp,
                    pendingIntent
                )
            }
        }
    }

    fun showExpiryNotification(item: InventoryItem, daysUntilExpiry: Int) {
        val intent = Intent(context, ExpiryAlertsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val notificationText = when {
            daysUntilExpiry < 0 -> "Warning: ${item.name} has expired! Do not use."
            daysUntilExpiry == 0 -> "Warning: ${item.name} expires today!"
            daysUntilExpiry == 1 -> "Warning: ${item.name} expires tomorrow!"
            else -> "Warning: ${item.name} expires in $daysUntilExpiry days."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Expiry Alert")
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_PREFIX + item.id.hashCode(), notification)
    }

    fun cancelNotificationsForItem(itemId: String) {
        val dailyIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.example.usebefore.DAILY_NOTIFICATION"
        }

        val expiryIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.example.usebefore.EXPIRY_NOTIFICATION"
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE
        }

        val dailyPendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_NOTIFICATION_REQUEST_CODE + itemId.hashCode(),
            dailyIntent,
            flags
        )

        val expiryPendingIntent = PendingIntent.getBroadcast(
            context,
            EXPIRY_NOTIFICATION_REQUEST_CODE + itemId.hashCode(),
            expiryIntent,
            flags
        )

        if (dailyPendingIntent != null) {
            alarmManager.cancel(dailyPendingIntent)
            dailyPendingIntent.cancel()
        }

        if (expiryPendingIntent != null) {
            alarmManager.cancel(expiryPendingIntent)
            expiryPendingIntent.cancel()
        }

        notificationManager.cancel(NOTIFICATION_ID_PREFIX + itemId.hashCode())
    }

    fun rescheduleAllNotifications(items: List<InventoryItem>) {
        for (item in items) {
            cancelNotificationsForItem(item.id)
        }

        val alertThresholdDays = preferenceHelper.getAlertThresholdDays()
        for (item in items) {
            scheduleDailyNotifications(item, alertThresholdDays)
            scheduleExpiryDateNotification(item)
        }
    }
}