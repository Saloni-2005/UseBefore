package com.example.usebefore.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usebefore.R
import com.example.usebefore.databinding.ActivitySignupRetailsBinding
import com.example.usebefore.viewmodel.DatabaseRetailsHelper

class SignupRetails : AppCompatActivity() {

    private lateinit var binding: ActivitySignupRetailsBinding
    private lateinit var databaseHelper: DatabaseRetailsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupRetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseRetailsHelper(this)

        binding.mybutton.setOnClickListener {
            val username = binding.myeditusername.text.toString()
            val password = binding.myeditpassword.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                signupUser(username, password)
            } else {
                Toast.makeText(this, "All fields are mandatory", Toast.LENGTH_SHORT).show()
            }
        }

        binding.mylogin.setOnClickListener {
            startActivity(Intent(this, LoginRetails::class.java))
            finish()
        }

    }

    private fun signupUser(username: String, password: String) {
        val insertRowid = databaseHelper.insertUser(username, password)
        if(insertRowid > -1){
            Toast.makeText(this, "Signup Successful", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginRetails::class.java))
            finish()
        }
        else{
            Toast.makeText(this, "Signup Failed", Toast.LENGTH_SHORT).show()
        }
    }
}