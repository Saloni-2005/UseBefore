package com.example.usebefore.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
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

        binding.guest.setOnClickListener {
            startActivity(Intent(this, HousesHomePage::class.java))
            finish()
        }

        val biometricManager = BiometricManager.from(this)
        when(biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)){
            BiometricManager.BIOMETRIC_SUCCESS -> {
                binding.fingerprint.setOnClickListener {
                    authenticate()
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    Toast.makeText(this , "Biometric not matched" , Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun authenticate(){
        val executor = ContextCompat.getMainExecutor(this)
        val bioometricPrompt = BiometricPrompt(this , executor , object : BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(applicationContext , "Fingerprint Authentication Successful" , Toast.LENGTH_SHORT).show()
                startActivity(Intent(applicationContext , HousesHomePage::class.java))
                finish()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(applicationContext ,"Finger not matched" , Toast.LENGTH_LONG).show()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(applicationContext , "Clean Sensor / fetal error : $errString" , Toast.LENGTH_LONG ).show()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Login using fingerprint")
            .setNegativeButtonText("Cancel")
            .build()

        bioometricPrompt.authenticate(promptInfo)
    }

    private fun validateLogin(username: String, password: String) {
        databaseReference.orderByChild("userName").equalTo(username).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(userSnapshot in snapshot.children) {
                        val userData = userSnapshot.getValue(HouseUsers::class.java)
                        if (userData != null && userData.password == password) {
                            Toast.makeText(applicationContext, "Login Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginHouses, HousesHomePage::class.java))
                            finish()
                            return
                        }
                    }
                }
                else{
                    Toast.makeText(this@LoginHouses, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LoginHouses, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}