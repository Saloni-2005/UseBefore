package com.example.usebefore.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.usebefore.R
import com.example.usebefore.databinding.ActivityAddItemsBinding
import android.app.DatePickerDialog
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.usebefore.repository.InventoryItem
import com.example.usebefore.repository.ItemStatus
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddItemsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddItemsBinding
    private lateinit var itemsAdapter: TempItemsAdapter
    private val itemsList = ArrayList<InventoryItem>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val calendar = Calendar.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupClickListeners()

    }
    private fun setupRecyclerView() {
        itemsAdapter = TempItemsAdapter(itemsList) { position ->
            itemsList.removeAt(position)
            itemsAdapter.notifyItemRemoved(position)
        }
        binding.itemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AddItemsActivity)
            adapter = itemsAdapter
        }
    }

    private fun setupClickListeners() {
        // Set up date picker for expiry date
        binding.expiryDateEditText.setOnClickListener {
            showDatePicker()
        }

        // Add item to temporary list
        binding.addToListButton.setOnClickListener {
            addItemToList()
        }

        // Save all items to Firebase
        binding.saveAllItemsButton.setOnClickListener {
            saveAllItems()
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    private fun updateDateInView() {
        binding.expiryDateEditText.setText(dateFormat.format(calendar.time))
    }

    private fun addItemToList() {
        val productName = binding.productNameEditText.text.toString().trim()
        val expiryDate = binding.expiryDateEditText.text.toString().trim()

        if (productName.isEmpty()) {
            binding.productNameLayout.error = "Product name cannot be empty"
            return
        }

        if (expiryDate.isEmpty()) {
            binding.expiryDateLayout.error = "Expiry date cannot be empty"
            return
        }

        // Clear errors
        binding.productNameLayout.error = null
        binding.expiryDateLayout.error = null

        // Create a new item and add it to the list
        val item = InventoryItem(
            id = UUID.randomUUID().toString(),
            name = productName,
            expiryDate = expiryDate,
            expiryTimestamp = calendar.timeInMillis,
            status = ItemStatus.ACTIVE,
            createdAt = System.currentTimeMillis()
        )

        itemsList.add(item)
        itemsAdapter.notifyItemInserted(itemsList.size - 1)

        // Clear input fields
        binding.productNameEditText.text?.clear()
        binding.expiryDateEditText.text?.clear()
    }

    private fun saveAllItems() {
        if (itemsList.isEmpty()) {
            Toast.makeText(this, "No items to save", Toast.LENGTH_SHORT).show()
            return
        }

        // Save each item to Firebase
        for (item in itemsList) {
            val itemRef = database.child("inventory").child(item.id)
            itemRef.setValue(item)
                .addOnSuccessListener {
                    // Do nothing for individual success
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save ${item.name}: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        Toast.makeText(this, "${itemsList.size} items saved successfully", Toast.LENGTH_SHORT).show()

        // Clear the list after successful save
        itemsList.clear()
        itemsAdapter.notifyDataSetChanged()

        // Navigate back or refresh
        finish()
    }
}

class TempItemsAdapter(
    private val items: List<InventoryItem>,
    private val onItemRemove: (position: Int) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<TempItemsAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ItemViewHolder {
        val binding = com.example.usebefore.databinding.ItemProductBinding.inflate(
            android.view.LayoutInflater.from(parent.context), parent, false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(private val binding: com.example.usebefore.databinding.ItemProductBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InventoryItem, position: Int) {
            binding.productNameTextView.text = item.name
            binding.expiryDateTextView.text = "Expires on: ${item.expiryDate}"

            binding.removeItemButton.setOnClickListener {
                onItemRemove(position)
            }
        }
    }
}