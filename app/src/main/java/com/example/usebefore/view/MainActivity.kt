package com.example.usebefore.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.usebefore.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.houses.setOnClickListener {
            val intent = Intent(this, LoginHouses::class.java)
            startActivity(intent)
        }

        binding.retailers.setOnClickListener {
            val intent = Intent(this, LoginRetails::class.java)
            startActivity(intent)
        }
    }
}