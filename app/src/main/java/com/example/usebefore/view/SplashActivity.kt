package com.example.usebefore.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.usebefore.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        val logoImageView = findViewById<ImageView>(R.id.splash_logo)
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_slide)
        logoImageView.startAnimation(animation)

        val sharedPreferences = getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        val isUserLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val isFingerprintEnabled = sharedPreferences.getBoolean("fingerprintEnabled", false)

        Handler(Looper.getMainLooper()).postDelayed({
            if (!isUserLoggedIn) {
                startActivity(Intent(this, LoginHouses::class.java))
                finish()
            } else if (isUserLoggedIn && isFingerprintEnabled) {
                val biometricManager = BiometricManager.from(this)
                when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                    BiometricManager.BIOMETRIC_SUCCESS -> {
                        authenticate()
                    }
                    else -> {
                        Toast.makeText(this, "Biometric authentication unavailable", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HousesHomePage::class.java))
                        finish()
                    }
                }
            } else {
                startActivity(Intent(this, HousesHomePage::class.java))
                finish()
            }
        }, 1500)
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

                when (errorCode) {
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_USER_CANCELED -> {
                        Toast.makeText(applicationContext, "Authentication cancelled", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(applicationContext, LoginHouses::class.java))
                        finish()
                    }
                    BiometricPrompt.ERROR_LOCKOUT,
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                        Toast.makeText(applicationContext,
                            "Too many failed attempts. Please login with password",
                            Toast.LENGTH_LONG).show()
                        startActivity(Intent(applicationContext, LoginHouses::class.java))
                        finish()
                    }
                    else -> {
                        Toast.makeText(applicationContext,
                            "Authentication error: $errString",
                            Toast.LENGTH_LONG).show()
                        startActivity(Intent(applicationContext, LoginHouses::class.java))
                        finish()
                    }
                }
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Login using fingerprint")
            .setNegativeButtonText("Use Password Instead")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}