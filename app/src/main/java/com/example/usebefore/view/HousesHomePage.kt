package com.example.usebefore.view

import android.content.Intent
import android.os.Bundle
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
    }
}