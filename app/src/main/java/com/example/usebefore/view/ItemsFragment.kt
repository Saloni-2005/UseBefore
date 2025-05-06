package com.example.usebefore.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.usebefore.R
import android.app.AlertDialog
import android.content.Intent
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.usebefore.databinding.FragmentItemsBinding
import com.example.usebefore.repository.InventoryItem
import com.example.usebefore.repository.ItemStatus
import com.example.usebefore.repository.NotificationManager
import com.example.usebefore.view.adapter.InventoryRecyclerAdapter
import com.google.firebase.database.*
import java.util.*

class ItemsFragment : Fragment() {

    private lateinit var notificationManager: NotificationManager

    private var _binding: FragmentItemsBinding? = null
    private val binding get() = _binding!!

    private lateinit var inventoryAdapter: InventoryRecyclerAdapter
    private val inventoryItems = mutableListOf<InventoryItem>()
    private lateinit var itemStatus: ItemStatus
    private lateinit var database: DatabaseReference

    companion object {
        private const val ARG_STATUS = "item_status"

        fun newInstance(status: ItemStatus): ItemsFragment {
            val fragment = ItemsFragment()
            val args = Bundle()
            args.putString(ARG_STATUS, status.name)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val statusName = it.getString(ARG_STATUS) ?: ItemStatus.ACTIVE.name
            itemStatus = ItemStatus.valueOf(statusName)
        }
        database = FirebaseDatabase.getInstance().reference.child("inventory")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadInventoryItems()
    }


    private fun setupRecyclerView() {
        inventoryAdapter = InventoryRecyclerAdapter(
            inventoryItems,
            onEditClicked = { item -> showEditDialog(item) },
            onDeleteClicked = { item -> showDeleteConfirmation(item) }
        )

        binding.itemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = inventoryAdapter
        }
    }

    private fun loadInventoryItems() {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyTextView.visibility = View.GONE

        val query = database.orderByChild("expiryTimestamp")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                inventoryItems.clear()

                // Check and update expired items
                val currentTime = System.currentTimeMillis()

                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(InventoryItem::class.java)
                    item?.let {
                        // Update item status if expired
                        if (it.status == ItemStatus.ACTIVE && it.expiryTimestamp < currentTime) {
                            it.status = ItemStatus.EXPIRED
                            database.child(it.id).child("status").setValue(ItemStatus.EXPIRED)
                        }

                        // Filter by status
                        if (it.status == itemStatus) {
                            inventoryItems.add(it)
                        }
                    }
                }

                inventoryAdapter.notifyDataSetChanged()

                binding.progressBar.visibility = View.GONE
                if (inventoryItems.isEmpty()) {
                    binding.emptyTextView.visibility = View.VISIBLE
                } else {
                    binding.emptyTextView.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showEditDialog(item: InventoryItem) {
        // Launch edit activity or show dialog for editing
        val intent = Intent(requireContext(), AddItemsActivity::class.java).apply {
            putExtra("ITEM_ID", item.id)
            putExtra("EDIT_MODE", true)
        }
        startActivity(intent)
    }

    private fun showDeleteConfirmation(item: InventoryItem) {
        val message = if (itemStatus == ItemStatus.ACTIVE) {
            "Are you sure you want to mark this item as used?"
        } else {
            "Are you sure you want to delete this item?"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Confirm")
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ ->
                if (itemStatus == ItemStatus.ACTIVE) {
                    // Mark as used
                    database.child(item.id).child("status").setValue(ItemStatus.USED)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Item marked as used", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Operation failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Delete permanently
                    database.child(item.id).removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Deletion failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}