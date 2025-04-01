package com.example.usebefore.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.usebefore.databinding.ActivitySignupHousesBinding
import com.example.usebefore.repository.HouseUsers
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SignupHouses : AppCompatActivity() {

    private lateinit var binding: ActivitySignupHousesBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupHousesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("HouseUsers")

        binding.mybutton.setOnClickListener {
            val username = binding.myeditusername.text.toString()
            val password = binding.myeditpassword.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                saveUserData(username, password)
            } else {
                Toast.makeText(this, "All fields are mandatory", Toast.LENGTH_SHORT).show()
            }
        }
        binding.mylogin.setOnClickListener {
            startActivity(Intent(this, LoginHouses::class.java))
            finish()
        }
    }
    private fun saveUserData(username: String, password: String) {
        databaseReference.orderByChild("userName").equalTo(username).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()){
                    val id = databaseReference.push().key
                    val userData = HouseUsers(id, username, password)
                    databaseReference.child(id!!).setValue(userData)
                    Toast.makeText(this@SignupHouses, "Signup Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@SignupHouses, LoginHouses::class.java))
                    finish()
                }else{
                    Toast.makeText(this@SignupHouses, "User already exists", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SignupHouses, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}