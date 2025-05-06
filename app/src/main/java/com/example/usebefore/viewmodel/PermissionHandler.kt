package com.example.usebefore.viewmodel

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class PermissionHandler(private val activity: AppCompatActivity) {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var onPermissionGrantedCallback: (() -> Unit)? = null

    init {
        setupPermissionLauncher()
    }

    private fun setupPermissionLauncher() {
        requestPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                onPermissionGrantedCallback?.invoke()
            }
        }
    }

    fun checkNotificationPermission(onPermissionGranted: () -> Unit) {
        this.onPermissionGrantedCallback = onPermissionGranted

        // For Android 13+ (API level 33), runtime notification permission is required
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    onPermissionGranted()
                }
                activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show a dialog explaining why notification permission is needed
                    showPermissionRationaleDialog()
                }
                else -> {
                    // Request the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            onPermissionGranted()
        }
    }

    private fun showPermissionRationaleDialog() {
        androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle("Notification Permission Required")
            .setMessage("To receive expiry alerts, the app needs permission to send notifications. Please grant this permission.")
            .setPositiveButton("Grant") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    companion object {
        fun hasNotificationPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }
    }
}