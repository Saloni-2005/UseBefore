package com.example.usebefore.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.usebefore.databinding.ActivityLoginHousesBinding
import com.example.usebefore.repository.HouseUsers
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginHouses : AppCompatActivity() {

    private lateinit var binding: ActivityLoginHousesBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginHousesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("HouseUsers")

        binding.mybutton.setOnClickListener {
            val username = binding.myeditusername.text.toString()
            val password = binding.myeditpassword.text.toString()

            if(username.isNotEmpty() && password.isNotEmpty()){
                validateLogin(username, password)
            }
            else{
                Toast.makeText(this, "All fields are mandatory", Toast.LENGTH_SHORT).show()
            }
        }

        binding.mysignup.setOnClickListener {
            startActivity(Intent(this, SignupHouses::class.java))
            finish()
        }
    }

    private fun validateLogin(username: String, password: String) {

        databaseReference.orderByChild("userName").equalTo(username).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                    for(userSnapshot in snapshot.children) {
                        val userData = userSnapshot.getValue(HouseUsers::class.java)

                        if (userData != null && userData.password == password) {
                            val sharedPreferences = getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)

                            val previousUser = sharedPreferences.getString("lastLoggedInUser", "")
                            val isNewUser = previousUser != username

                            val editor = sharedPreferences.edit()
                            editor.putBoolean("isLoggedIn", true)
                            editor.putString("username", userData.userName)

                            if (isNewUser) {
                                userData.userName?.let { resetProfileData(editor, it) }
                            }

                            editor.putString("lastLoggedInUser", username)
                            editor.apply()

                            Toast.makeText(applicationContext, "Login Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginHouses, HousesHomePage::class.java))
                            finish()
                            return
                        }
                    }
                    Toast.makeText(this@LoginHouses, "Invalid password", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this@LoginHouses, "Username not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LoginHouses, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun resetProfileData(editor: SharedPreferences.Editor, username: String) {
        // Clear personal information but keep system settings
        editor.putString("email", "")
        editor.putString("phone", "")
        editor.putString("location", "")
        editor.putString("profileImageUri", null)

        // Set default values for a new user
        editor.putBoolean("isPremium", false)

        val sharedPreferences = getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)

        if (!sharedPreferences.contains("notifications")) {
            editor.putBoolean("notifications", true) // Default ON
        }

        if (!sharedPreferences.contains("darkMode")) {
            editor.putBoolean("darkMode", false) // Default OFF
        }

        if (!sharedPreferences.contains("fingerprintEnabled")) {
            editor.putBoolean("fingerprintEnabled", false) // Default OFF
        }
    }
}