package com.example.usebefore.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.usebefore.databinding.ActivityHousesHomePageBinding
import com.google.android.material.navigation.NavigationView

class HousesHomePage : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private lateinit var menuIcon: ImageView
    private lateinit var binding: ActivityHousesHomePageBinding

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHousesHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = binding.drawerLayout
        navigationView = binding.navView
        menuIcon = binding.menuIcon

        // Setup navigation drawer item selection
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                com.example.usebefore.R.id.nav_home -> {
                    // Load home content
                    supportFragmentManager.beginTransaction()
                        .replace(com.example.usebefore.R.id.fragment_container, HomeFragment())
                        .commit()
                    true
                }
                com.example.usebefore.R.id.nav_info -> {
                    supportFragmentManager.beginTransaction()
                        .replace(com.example.usebefore.R.id.fragment_container, InfoFragment())
                        .addToBackStack(null)
                        .commit()
                    true
                }
                com.example.usebefore.R.id.nav_help -> {
                    supportFragmentManager.beginTransaction()
                        .replace(com.example.usebefore.R.id.fragment_container, HelpFragment())
                        .addToBackStack(null)
                        .commit()
                    true
                }
                com.example.usebefore.R.id.nav_upgrade -> {
                    supportFragmentManager.beginTransaction()
                        .replace(com.example.usebefore.R.id.fragment_container, UpgradeFragment())
                        .addToBackStack(null)
                        .commit()
                    true
                }
                com.example.usebefore.R.id.nav_logout -> {
                    // Implement logout logic
                    true
                }
                else -> false
            }

            // Close drawer after selection
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Set click listener for menu icon
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Load initial fragment if it's first launch
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(com.example.usebefore.R.id.fragment_container, HomeFragment())
                .commit()
        }

        binding.profileview.setOnClickListener {
            val intent = Intent(this, ProfilePageHouse::class.java)
            startActivity(intent)
        }

        loadProfileImage()
    }

    override fun onBackPressed() {
        // Close drawer on back press if open
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
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
                binding.profile.setImageResource(com.example.usebefore.R.drawable.baseline_person_24)
            }
        } else {
            android.util.Log.d("HousesHomePage", "No saved profile image URI found, using default")
            binding.profile.setImageResource(com.example.usebefore.R.drawable.baseline_person_24)
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfileImage()
    }
}