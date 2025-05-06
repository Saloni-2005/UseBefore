package com.example.usebefore.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.usebefore.R
import com.example.usebefore.repository.InventoryItem
import com.example.usebefore.repository.ItemStatus
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.*
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    private lateinit var totalItemsTextView: TextView
    private lateinit var expiringSoonTextView: TextView
    private lateinit var database: DatabaseReference
    private val ALERT_DAYS_THRESHOLD = 6 // Same threshold as in ExpiryAlertsActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().reference.child("inventory")

        // Reference to the TextViews displaying counts
        // Fix: Direct access to TextViews instead of trying to cast LinearLayout to MaterialCardView
        totalItemsTextView = view.findViewById(R.id.total_items_count)
        expiringSoonTextView = view.findViewById(R.id.expiring_soon_count)

        // Load item counts
        loadItemCounts()

        view.findViewById<MaterialCardView>(R.id.view_inventory).setOnClickListener {
            startActivity(Intent(requireContext(), ViewInventoryActivity::class.java))
        }

        val scanBarcodeCard = view.findViewById<MaterialCardView>(R.id.scan_barcode)
        scanBarcodeCard.setOnClickListener {
            val intent = Intent(requireContext(), BarcodeScannerActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<MaterialCardView>(R.id.add_item).setOnClickListener {
            startActivity(Intent(requireContext(), AddItemsActivity::class.java))
        }

        view.findViewById<MaterialCardView>(R.id.alerts).setOnClickListener {
            startActivity(Intent(requireContext(), ExpiryAlertsActivity::class.java))
        }

        view.findViewById<MaterialCardView>(R.id.settingsview).setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
    }

    private fun loadItemCounts() {
        val query = database.orderByChild("expiryTimestamp")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var activeCount = 0
                var expiringSoonCount = 0

                val currentTime = System.currentTimeMillis()
                val thresholdMillis = TimeUnit.DAYS.toMillis(ALERT_DAYS_THRESHOLD.toLong())
                val alertThreshold = currentTime + thresholdMillis

                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(InventoryItem::class.java)
                    item?.let {
                        if (it.status == ItemStatus.ACTIVE) {
                            activeCount++

                            // Check if item is expiring soon
                            if (it.expiryTimestamp <= alertThreshold) {
                                expiringSoonCount++
                            }
                        }
                    }
                }

                // Update UI
                totalItemsTextView.text = activeCount.toString()
                expiringSoonTextView.text = expiringSoonCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}