package com.example.usebefore.view

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
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

        sharedPreferences = getSharedPreferences("ProfilePrefs" , Context.MODE_PRIVATE)

        setupListeners()

        loadUserDetails()
    }

    private fun setupListeners(){

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

        binding.switchFingerprint.setOnClickListener {
            if (isEditMode) {
                if (binding.switchFingerprint.isChecked) {
                    checkBiometricSupport()
                }
            } else {
                binding.switchFingerprint.isChecked = sharedPreferences.getBoolean("fingerprintEnabled", false)
                Toast.makeText(this, "Enable edit mode to change settings", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkBiometricSupport() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(this, "This device doesn't support fingerprint authentication", Toast.LENGTH_LONG).show()
                binding.switchFingerprint.isChecked = false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(this, "Biometric features are currently unavailable", Toast.LENGTH_LONG).show()
                binding.switchFingerprint.isChecked = false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(this, "No fingerprints enrolled. Please register at least one fingerprint in your device settings", Toast.LENGTH_LONG).show()
                binding.switchFingerprint.isChecked = false
            }
            else -> {
                Toast.makeText(this, "Biometric authentication unavailable", Toast.LENGTH_LONG).show()
                binding.switchFingerprint.isChecked = false
            }
        }
    }

    private fun loadUserDetails(){
        binding.txtUsername.text = sharedPreferences.getString("username", "Add your username")
        val isPremium = sharedPreferences.getBoolean("isPremium", false)
        binding.txtMembershipStatus.text = if (isPremium) "Premium Member" else "Not a Premium Member"

        // Load account information data
        val email = sharedPreferences.getString("email", "")
        val phone = sharedPreferences.getString("phone", "")
        val location = sharedPreferences.getString("location", "")

        // Set to both display and edit fields
        binding.txtEmail.setText(email)
        binding.txtEmailDisplay.text = email

        binding.txtPhone.setText(phone)
        binding.txtPhoneDisplay.text = phone

        binding.txtLocation.setText(location)
        binding.txtLocationDisplay.text = location

        // Load preferences
        binding.switchFingerprint.isChecked = sharedPreferences.getBoolean("fingerprintEnabled", false)
        binding.switchDarkMode.isChecked = sharedPreferences.getBoolean("darkMode", false)

        val profileImageUriString = sharedPreferences.getString("profileImageUri", null)
        if (profileImageUriString != null) {
            try {
                val profileImageUri = Uri.parse(profileImageUriString)
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, profileImageUri)
                binding.profileImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun enableEditMode() {
        isEditMode = true

        originalValues["username"] = binding.txtUsername.text.toString()
        originalValues["email"] = binding.txtEmailDisplay.text.toString()
        originalValues["phone"] = binding.txtPhoneDisplay.text.toString()
        originalValues["location"] = binding.txtLocationDisplay.text.toString()

        binding.txtEmailDisplay.visibility = View.GONE
        binding.txtEmail.visibility = View.VISIBLE

        binding.txtPhoneDisplay.visibility = View.GONE
        binding.txtPhone.visibility = View.VISIBLE

        binding.txtLocationDisplay.visibility = View.GONE
        binding.txtLocation.visibility = View.VISIBLE

        binding.btnEditProfile.text = "Save Changes"

        Toast.makeText(this, "Edit mode enabled", Toast.LENGTH_SHORT).show()
    }

    private fun disableEditMode() {
        isEditMode = false

        binding.txtEmailDisplay.visibility = View.VISIBLE
        binding.txtEmail.visibility = View.GONE

        binding.txtPhoneDisplay.visibility = View.VISIBLE
        binding.txtPhone.visibility = View.GONE

        binding.txtLocationDisplay.visibility = View.VISIBLE
        binding.txtLocation.visibility = View.GONE

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

        val username = binding.txtUsername.text.toString()
        val email = binding.txtEmail.text.toString()
        val phone = binding.txtPhone.text.toString()
        val location = binding.txtLocation.text.toString()

        editor.putString("username", username)
        editor.putString("email", email)
        editor.putString("phone", phone)
        editor.putString("location", location)

        editor.putBoolean("darkMode", binding.switchDarkMode.isChecked)
        editor.putBoolean("fingerprintEnabled", binding.switchFingerprint.isChecked)

        tempProfileUri?.let {
            editor.putString("profileImageUri", it.toString())
        }

        editor.apply()

        binding.txtUsername.text = username
        binding.txtEmailDisplay.text = email
        binding.txtPhoneDisplay.text = phone
        binding.txtLocationDisplay.text = location

        disableEditMode()
        Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show()
    }

    private fun discardChanges() {
        binding.txtUsername.text = originalValues["username"]
        binding.txtEmail.setText(originalValues["email"])
        binding.txtPhone.setText(originalValues["phone"])
        binding.txtLocation.setText(originalValues["location"])

        // Reset temp profile image
        tempProfileUri = null

        // Restore original profile image
        val profileImageUriString = sharedPreferences.getString("profileImageUri", null)
        if (profileImageUriString != null) {
            try {
                val profileImageUri = Uri.parse(profileImageUriString)
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, profileImageUri)
                binding.profileImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
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