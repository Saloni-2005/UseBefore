package com.example.usebefore.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.usebefore.databinding.ActivityHousesHomePageBinding
import com.google.android.material.navigation.NavigationView
import android.app.AlertDialog

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
                    showLogoutConfirmationDialog()
                    true
                }
                else -> false
            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

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
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun loadProfileImage() {
        val sharedPreferences = getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        val profileImageUriString = sharedPreferences.getString("profileImageUri", null)

        android.util.Log.d("HousesHomePage", "Loading profile image URI: $profileImageUriString")

        if (profileImageUriString != null) {
            try {
                val profileImageUri = Uri.parse(profileImageUriString)
                try {
                    contentResolver.takePersistableUriPermission(
                        profileImageUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    android.util.Log.e("HousesHomePage", "Failed to take permission: ${e.message}")
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

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                logout()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun logout() {
        val sharedPreferences = getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)

        sharedPreferences.edit().apply {
            putBoolean("isLoggedIn", false)
            apply()
        }

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginHouses::class.java)
        // Clear back stack so user can't go back to the home page after logout
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        loadProfileImage()
    }
}