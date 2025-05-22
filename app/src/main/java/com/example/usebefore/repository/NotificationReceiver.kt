package com.example.usebefore.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.usebefore.repository.InventoryItem
import com.example.usebefore.repository.NotificationManager
import com.example.usebefore.viewmodel.PreferenceHelper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationType = intent.getStringExtra("NOTIFICATION_TYPE") ?: return
        val itemId = intent.getStringExtra("ITEM_ID") ?: return
        val itemName = intent.getStringExtra("ITEM_NAME") ?: "An item"
        val expiryDate = intent.getLongExtra("EXPIRY_DATE", 0)

        val database = FirebaseDatabase.getInstance().reference.child("inventory").child(itemId)

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val item = snapshot.getValue(InventoryItem::class.java)

                // Only show notification if item still exists and is active
                if (item != null) {
                    val notificationManager = NotificationManager(context)
                    val preferenceHelper = PreferenceHelper(context)

                    // Only proceed if notifications are enabled
                    if (!preferenceHelper.isNotificationsEnabled()) {
                        return
                    }

                    when (notificationType) {
                        "DAILY" -> {
                            val currentTime = System.currentTimeMillis()
                            val daysUntilExpiry = TimeUnit.MILLISECONDS.toDays(item.expiryTimestamp - currentTime).toInt()
                            val alertThresholdDays = preferenceHelper.getAlertThresholdDays()

                            // Show notification for ANY item within or equal to threshold days
                            if (daysUntilExpiry >= 0 && daysUntilExpiry <= alertThresholdDays) {
                                notificationManager.showExpiryNotification(item, daysUntilExpiry)
                            }

                            // Schedule the next daily notification if item hasn't expired yet
                            if (daysUntilExpiry >= 0) {
                                val notificationTimeHour = preferenceHelper.getNotificationTimeHour()
                                val notificationTimeMinute = preferenceHelper.getNotificationTimeMinute()

                                scheduleTomorrowNotification(
                                    context,
                                    item,
                                    notificationTimeHour,
                                    notificationTimeMinute
                                )
                            }
                        }
                        "EXPIRED" -> {
                            // Show expiry notification
                            notificationManager.showExpiryNotification(item, 0)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error - you might want to log this or retry
            }
        })
    }

    private fun scheduleTomorrowNotification(
        context: Context,
        item: InventoryItem,
        hour: Int,
        minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Create intent for tomorrow's notification
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.example.usebefore.DAILY_NOTIFICATION"
            putExtra("NOTIFICATION_TYPE", "DAILY")
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
            NotificationManager.DAILY_NOTIFICATION_REQUEST_CODE + item.id.hashCode(),
            intent,
            flags
        )

        // Set calendar for tomorrow at the specified time
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.DAY_OF_YEAR, 1)  // Tomorrow
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Check if the item will still be within alert threshold tomorrow
        val tomorrowTime = calendar.timeInMillis
        val daysUntilExpiryTomorrow = TimeUnit.MILLISECONDS.toDays(item.expiryTimestamp - tomorrowTime).toInt()

        // Only schedule if item will still be relevant tomorrow (not expired)
        if (daysUntilExpiryTomorrow >= 0) {
            // Schedule the alarm using appropriate method for the API level
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                // For Android 12+ without SCHEDULE_EXACT_ALARM permission
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                // For older versions
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }
}