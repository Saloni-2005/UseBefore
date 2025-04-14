package com.example.usebefore.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usebefore.R
import com.example.usebefore.databinding.ActivityHousesHomePageBinding

class HousesHomePage : AppCompatActivity() {

    private lateinit var binding: ActivityHousesHomePageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHousesHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.profileview.setOnClickListener {
            val intent = Intent(this, ProfilePageHouse::class.java)
            startActivity(intent)
        }

        loadProfileImage()
    }

    // In HousesHomePage.kt
    private fun loadProfileImage() {
        val sharedPreferences = getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        val profileImageUriString = sharedPreferences.getString("profileImageUri", null)

        android.util.Log.d("HousesHomePage", "Loading profile image URI: $profileImageUriString")

        if (profileImageUriString != null) {
            try {
                val profileImageUri = Uri.parse(profileImageUriString)
                // Take a content URI permission to ensure persistence
                try {
                    contentResolver.takePersistableUriPermission(
                        profileImageUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    android.util.Log.e("HousesHomePage", "Failed to take permission: ${e.message}")
                    // Continue anyway, might still work
                }

                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, profileImageUri)
                binding.profile.setImageBitmap(bitmap)
                android.util.Log.d("HousesHomePage", "Successfully loaded image from URI")
            } catch (e: Exception) {
                android.util.Log.e("HousesHomePage", "Error loading image: ${e.message}")
                e.printStackTrace()
                binding.profile.setImageResource(R.drawable.baseline_person_24)
            }
        } else {
            android.util.Log.d("HousesHomePage", "No saved profile image URI found, using default")
            binding.profile.setImageResource(R.drawable.baseline_person_24)
        }
    }

    override fun onResume() {
        super.onResume()

        loadProfileImage()
    }
}