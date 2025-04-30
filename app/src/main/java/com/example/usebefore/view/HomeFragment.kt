package com.example.usebefore.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.usebefore.R
import com.google.android.material.card.MaterialCardView

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up click listeners for your cards
        view.findViewById<MaterialCardView>(R.id.view_inventory).setOnClickListener {
            // Handle inventory view
        }

        val scanBarcodeCard = view.findViewById<MaterialCardView>(R.id.scan_barcode)
        scanBarcodeCard.setOnClickListener {
            val intent = Intent(requireContext(), BarcodeScannerActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<MaterialCardView>(R.id.add_item).setOnClickListener {
            // Handle add item
        }

        view.findViewById<MaterialCardView>(R.id.alerts).setOnClickListener {
            // Handle alerts
        }

        view.findViewById<MaterialCardView>(R.id.settingsview).setOnClickListener {
            // Handle settings
        }
    }
}