package com.example.usebefore.view

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.usebefore.databinding.ActivitySettingsBinding
import com.example.usebefore.repository.NotificationManager
import com.example.usebefore.repository.InventoryItem
import com.example.usebefore.repository.ItemStatus
import com.example.usebefore.viewmodel.PermissionHandler
import com.example.usebefore.viewmodel.PreferenceHelper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize helpers
        preferenceHelper = PreferenceHelper(this)
        notificationManager = NotificationManager(this)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"

        // Load and display current settings
        loadSettings()

        // Setup listeners
        setupNotificationToggle()
        setupNotificationTimeSelector()
        setupAlertThresholdSeekBar()
        setupSaveButton()

        notificationManager.createNotificationChannel()
    }

    private fun loadSettings() {
        // Load notification enabled/disabled state
        binding.switchEnableNotifications.isChecked = preferenceHelper.isNotificationsEnabled()

        // Load notification time
        updateNotificationTimeDisplay()

        // Load alert threshold
        val alertThreshold = preferenceHelper.getAlertThresholdDays()
        binding.seekbarAlertDays.progress = alertThreshold
        updateAlertDaysText(alertThreshold)
    }

    private fun updateNotificationTimeDisplay() {
        val hour = preferenceHelper.getNotificationTimeHour()
        val minute = preferenceHelper.getNotificationTimeMinute()

        // Format time (AM/PM format)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        binding.textNotificationTime.text = timeFormat.format(calendar.time)
    }

    private fun setupNotificationToggle() {
        val permissionHandler = PermissionHandler(this)

        binding.switchEnableNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Check for notification permission when enabling notifications
                permissionHandler.checkNotificationPermission {
                    // Permission granted, enable notifications
                    preferenceHelper.setNotificationsEnabled(true)
                    binding.switchEnableNotifications.isChecked = true
                }
            } else {
                // Just disable notifications
                preferenceHelper.setNotificationsEnabled(false)
            }
        }
    }

    private fun setupNotificationTimeSelector() {
        binding.layoutNotificationTime.setOnClickListener {
            val hour = preferenceHelper.getNotificationTimeHour()
            val minute = preferenceHelper.getNotificationTimeMinute()

            TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    // Save the new time
                    preferenceHelper.setNotificationTime(selectedHour, selectedMinute)

                    // Update the display
                    updateNotificationTimeDisplay()
                },
                hour,
                minute,
                false // 12-hour format
            ).show()
        }
    }

    private fun setupAlertThresholdSeekBar() {
        binding.seekbarAlertDays.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Make minimum 1 day
                val adjustedProgress = if (progress < 1) 1 else progress
                updateAlertDaysText(adjustedProgress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val progress = seekBar?.progress ?: return
                // Make minimum 1 day
                val adjustedProgress = if (progress < 1) 1 else progress
                seekBar.progress = adjustedProgress
                preferenceHelper.setAlertThresholdDays(adjustedProgress)
            }
        })
    }

    private fun updateAlertDaysText(days: Int) {
        binding.textAlertDays.text = when (days) {
            1 -> "1 day before expiry"
            else -> "$days days before expiry"
        }
    }

    private fun setupSaveButton() {
        binding.buttonSaveSettings.setOnClickListener {
            // Save settings
            preferenceHelper.setNotificationsEnabled(binding.switchEnableNotifications.isChecked)
            preferenceHelper.setAlertThresholdDays(binding.seekbarAlertDays.progress)

            if (binding.switchEnableNotifications.isChecked) {
                fetchAndRescheduleNotifications()
            } else {
                fetchAndCancelNotifications()
            }

            android.widget.Toast.makeText(this, "Settings saved", android.widget.Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fetchAndRescheduleNotifications() {
        val database = FirebaseDatabase.getInstance().reference.child("inventory")
        database.orderByChild("status").equalTo(ItemStatus.ACTIVE.name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val items = mutableListOf<InventoryItem>()
                    for (itemSnapshot in snapshot.children) {
                        val item = itemSnapshot.getValue(InventoryItem::class.java)
                        item?.let { items.add(it) }
                    }

                    // Cancel all existing notifications first
                    for (item in items) {
                        notificationManager.cancelNotificationsForItem(item.id)
                    }

                    // Schedule fresh notifications for all items
                    for (item in items) {
                        notificationManager.scheduleNotificationsForItem(item)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    private fun fetchAndCancelNotifications() {
        val database = FirebaseDatabase.getInstance().reference.child("inventory")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(InventoryItem::class.java)
                    item?.let { notificationManager.cancelNotificationsForItem(it.id) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}