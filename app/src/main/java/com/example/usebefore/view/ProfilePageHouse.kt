package com.example.usebefore.view

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usebefore.R
import com.example.usebefore.databinding.ActivityProfilePageHouseBinding

class ProfilePageHouse : AppCompatActivity() {

    private lateinit var binding: ActivityProfilePageHouseBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var isEditMode = false
    private var tempProfileUri : Uri? = null
    private var originalValues = HashMap<String,String>()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val uri = result.data?.data
            uri?.let{
                tempProfileUri = it
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver , it)
                    binding.profileImage.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext , "Failed to load image" , Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfilePageHouseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            startActivity(Intent(this@ProfilePageHouse, HousesHomePage::class.java))
        }

        sharedPreferences = getSharedPreferences("ProfilePrefs" , Context.MODE_PRIVATE)

        setupListeners()

        loadUserDetails()
    }

    private fun setupListeners(){
        binding.btnBack.setOnClickListener {
            if(isEditMode){
                showDiscardChangesDialog()
            }
            else{
                finish()
            }
        }

        binding.btnChangeProfilePic.setOnClickListener {
            if(isEditMode){
                openImagePicker()
            }
            else{
                Toast.makeText(this , "Enable edit mode to change profile picture" , Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnEditProfile.setOnClickListener {
            if (!isEditMode) {
                enableEditMode()
            } else {
                showSaveChangesDialog()
            }
        }


    }




    private fun loadUserDetails(){
        binding.txtUsername.text = sharedPreferences.getString("username", "Add your username")
        binding.txtEmail.setText(sharedPreferences.getString("email", ""))
        binding.txtPhone.setText(sharedPreferences.getString("phone", ""))
        binding.txtLocation.setText(sharedPreferences.getString("location", ""))

        // Load premium status
        val isPremium = sharedPreferences.getBoolean("isPremium", false)
        binding.txtMembershipStatus.text = if (isPremium) "Premium Member" else "Not a Premium Member"

        // Load preferences
        binding.switchNotifications.isChecked = sharedPreferences.getBoolean("notifications", true)
        binding.switchDarkMode.isChecked = sharedPreferences.getBoolean("darkMode", false)

        // Load profile image if available
        val profileImageUriString = sharedPreferences.getString("profileImageUri", null)
        if (profileImageUriString != null) {
            try {
                val profileImageUri = Uri.parse(profileImageUriString)
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, profileImageUri)
                binding.profileImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                // Use default image if loading fails
            }
        }
    }

    private fun enableEditMode() {
        isEditMode = true

        // Store original values in case user cancels
        originalValues["username"] = binding.txtUsername.text.toString()
        originalValues["email"] = binding.txtEmail.text.toString()
        originalValues["phone"] = binding.txtPhone.text.toString()
        originalValues["location"] = binding.txtLocation.text.toString()

        // Enable editing fields
        binding.txtEmail.isEnabled = true
        binding.txtPhone.isEnabled = true
        binding.txtLocation.isEnabled = true

        // Change button text
        binding.btnEditProfile.text = "Save Changes"

        Toast.makeText(this, "Edit mode enabled", Toast.LENGTH_SHORT).show()
    }

    private fun disableEditMode() {
        isEditMode = false

        // Disable editing fields
        binding.txtEmail.isEnabled = false
        binding.txtPhone.isEnabled = false
        binding.txtLocation.isEnabled = false

        // Change button text back
        binding.btnEditProfile.text = "Edit Profile"
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        pickImageLauncher.launch(intent)
    }

    private fun saveChanges() {
        val editor = sharedPreferences.edit()

        // Save text fields
        val username = binding.txtUsername.text.toString()
        val email = binding.txtEmail.text.toString()
        val phone = binding.txtPhone.text.toString()
        val location = binding.txtLocation.text.toString()

        editor.putString("username", username)
        editor.putString("email", email)
        editor.putString("phone", phone)
        editor.putString("location", location)

        // Save preferences
        editor.putBoolean("notifications", binding.switchNotifications.isChecked)
        editor.putBoolean("darkMode", binding.switchDarkMode.isChecked)

        // Save profile image URI if changed
        tempProfileUri?.let {
            editor.putString("profileImageUri", it.toString())
        }

        editor.apply()

        // Update the username text view
        binding.txtUsername.text = username

        disableEditMode()
        Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show()
    }

    private fun discardChanges() {
        // Restore original values
        binding.txtUsername.text = originalValues["username"]
        binding.txtEmail.setText(originalValues["email"])
        binding.txtPhone.setText(originalValues["phone"])
        binding.txtLocation.setText(originalValues["location"])

        // Reset temp profile image
        tempProfileUri = null

        // Reload profile image from SharedPreferences
        val profileImageUriString = sharedPreferences.getString("profileImageUri", null)
        if (profileImageUriString != null) {
            try {
                val profileImageUri = Uri.parse(profileImageUriString)
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, profileImageUri)
                binding.profileImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                // Use default image if loading fails
            }
        } else {
            // Set default image
            binding.profileImage.setImageResource(R.drawable.profile_placeholder)
        }

        disableEditMode()
        Toast.makeText(this, "Changes discarded", Toast.LENGTH_SHORT).show()
    }

    private fun showSaveChangesDialog() {
        AlertDialog.Builder(this)
            .setTitle("Save Changes")
            .setMessage("Do you want to save your changes?")
            .setPositiveButton("Save") { _, _ ->
                saveChanges()
            }
            .setNegativeButton("Discard") { _, _ ->
                discardChanges()
            }
            .setCancelable(true)
            .show()
    }

    private fun showDiscardChangesDialog() {
        AlertDialog.Builder(this)
            .setTitle("Discard Changes")
            .setMessage("You have unsaved changes. Do you want to discard them?")
            .setPositiveButton("Discard") { _, _ ->
                binding.profileImage.setImageResource(R.drawable.profile_placeholder)
                isEditMode = false
                finish()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    override fun onBackPressed() {
        if (isEditMode) {
            showDiscardChangesDialog()
        } else {
            super.onBackPressed()
        }
    }
}