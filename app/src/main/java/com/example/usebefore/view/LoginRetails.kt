package com.example.usebefore.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usebefore.R
import com.example.usebefore.databinding.ActivityLoginRetailsBinding
import com.example.usebefore.viewmodel.DatabaseRetailsHelper

class LoginRetails : AppCompatActivity() {

    private lateinit var binding: ActivityLoginRetailsBinding
    private lateinit var databaseHelper: DatabaseRetailsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginRetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseRetailsHelper(this)

        binding.mybutton.setOnClickListener {
            val username = binding.myeditusername.text.toString()
            val password = binding.myeditpassword.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                loginUser(username, password)
            } else {
                Toast.makeText(this, "All fields are mandatory", Toast.LENGTH_SHORT).show()
            }
        }

        binding.mysignup.setOnClickListener {
            startActivity(Intent(this, SignupRetails::class.java))
            finish()
        }
    }

    private fun loginUser(username: String, password: String) {
        val userExists = databaseHelper.getUser(username, password)

        if (userExists) {
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, RetailHomePage::class.java))
            finish()
        } else {
            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
        }
    }
}