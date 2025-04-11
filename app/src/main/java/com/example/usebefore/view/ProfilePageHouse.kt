package com.example.usebefore.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usebefore.R
import com.example.usebefore.databinding.ActivityProfilePageHouseBinding

class ProfilePageHouse : AppCompatActivity() {

    private lateinit var binding: ActivityProfilePageHouseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfilePageHouseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            startActivity(Intent(this@ProfilePageHouse, HousesHomePage::class.java))
        }
    }
}