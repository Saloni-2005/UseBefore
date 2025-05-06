package com.example.usebefore.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.usebefore.databinding.ActivityExpiryAlertsBinding
import com.example.usebefore.repository.InventoryItem
import com.example.usebefore.repository.ItemStatus
import com.example.usebefore.view.adapter.InventoryRecyclerAdapter
import com.google.firebase.database.*
import java.util.*
import java.util.concurrent.TimeUnit

class ExpiryAlertsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpiryAlertsBinding
    private lateinit var inventoryAdapter: InventoryRecyclerAdapter
    private val alertItems = mutableListOf<InventoryItem>()
    private lateinit var database: DatabaseReference

    private val ALERT_DAYS_THRESHOLD = 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpiryAlertsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Expiry Alerts"

        database = FirebaseDatabase.getInstance().reference.child("inventory")

        setupRecyclerView()
        loadAlertItems()
    }

    private fun setupRecyclerView() {
        inventoryAdapter = InventoryRecyclerAdapter(
            alertItems,
            onEditClicked = { item ->
                val intent = android.content.Intent(this, AddItemsActivity::class.java).apply {
                    putExtra("ITEM_ID", item.id)
                    putExtra("EDIT_MODE", true)
                }
                startActivity(intent)
            },
            onDeleteClicked = { item -> showDeleteConfirmation(item) }
        )

        binding.alertsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ExpiryAlertsActivity)
            adapter = inventoryAdapter
        }
    }

    private fun loadAlertItems() {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyTextView.visibility = View.GONE

        val query = database.orderByChild("expiryTimestamp")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                alertItems.clear()

                val currentTime = System.currentTimeMillis()
                val thresholdMillis = TimeUnit.DAYS.toMillis(ALERT_DAYS_THRESHOLD.toLong())
                val alertThreshold = currentTime + thresholdMillis

                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(InventoryItem::class.java)
                    item?.let {
                        // Only include active items that expire within the threshold
                        if (it.status == ItemStatus.ACTIVE &&
                            it.expiryTimestamp <= alertThreshold) {
                            alertItems.add(it)
                        }
                    }
                }

                // Sort by expiry date (soonest first)
                alertItems.sortBy { it.expiryTimestamp }

                inventoryAdapter.notifyDataSetChanged()

                binding.progressBar.visibility = View.GONE
                if (alertItems.isEmpty()) {
                    binding.emptyTextView.visibility = View.VISIBLE
                } else {
                    binding.emptyTextView.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@ExpiryAlertsActivity,
                    "Failed to load alerts: ${error.message}",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteConfirmation(item: InventoryItem) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Mark as Used")
            .setMessage("Do you want to mark this item as used?")
            .setPositiveButton("Yes") { _, _ ->
                // Mark as used
                database.child(item.id).child("status").setValue(ItemStatus.USED)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Item marked as used", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Operation failed: ${e.message}",
                            Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}