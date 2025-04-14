package com.example.usebefore.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usebefore.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)


        val sharedPreferences = getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        val isFingerprintEnabled = sharedPreferences.getBoolean("fingerprintEnabled", false)

        // Delay to show splash screen
        Handler(Looper.getMainLooper()).postDelayed({
            if (isFingerprintEnabled) {
                // Check biometric availability
                val biometricManager = BiometricManager.from(this)
                when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                    BiometricManager.BIOMETRIC_SUCCESS -> {
                        // Device supports biometric and has fingerprints enrolled
                        authenticate()
                    }
                    else -> {
                        // Skip authentication if biometric not available and go directly to home
                        Toast.makeText(this, "Biometric authentication unavailable", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HousesHomePage::class.java))
                        finish()
                    }
                }
            } else {
                // If fingerprint is not enabled, go directly to home
                startActivity(Intent(this, HousesHomePage::class.java))
                finish()
            }
        }, 1500) // Show splash screen for 1.5 seconds
    }

    private fun authenticate() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(applicationContext, "Fingerprint Authentication Successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(applicationContext, HousesHomePage::class.java))
                finish()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(applicationContext, "Finger not matched", Toast.LENGTH_LONG).show()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(applicationContext, "Clean Sensor / fatal error: $errString", Toast.LENGTH_LONG).show()

                // If there's an error (like user cancellation), still allow them to proceed to the app
                // This prevents the user from being locked out completely
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                    Toast.makeText(applicationContext, "Authentication cancelled", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(applicationContext, HousesHomePage::class.java))
                    finish()
                }
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Login using fingerprint")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}