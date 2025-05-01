package com.example.usebefore.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.usebefore.databinding.ItemInventoryProductBinding
import com.example.usebefore.repository.ExpiryAlertColor
import com.example.usebefore.repository.InventoryItem
import com.example.usebefore.repository.ItemStatus

class InventoryRecyclerAdapter(
    private val items: List<InventoryItem>,
    private val onEditClicked: (InventoryItem) -> Unit,
    private val onDeleteClicked: (InventoryItem) -> Unit
) : RecyclerView.Adapter<InventoryRecyclerAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemInventoryProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(private val binding: ItemInventoryProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InventoryItem) {
            binding.productNameTextView.text = item.name
            binding.expiryDateTextView.text = "Expires on: ${item.expiryDate}"

            // Set expiry status text
            val daysUntilExpiry = item.daysUntilExpiry()
            binding.expiryStatusTextView.text = when {
                item.status == ItemStatus.EXPIRED -> "EXPIRED"
                item.status == ItemStatus.USED -> "USED"
                daysUntilExpiry <= 0 -> "Expires today"
                daysUntilExpiry == 1 -> "Expires tomorrow"
                else -> "$daysUntilExpiry days remaining"
            }

            // Set text color based on expiry status
            if (item.status == ItemStatus.ACTIVE) {
                when (item.getExpiryAlertColor()) {
                    ExpiryAlertColor.RED -> {
                        binding.expiryStatusTextView.setTextColor(0xFFFF0000.toInt())
                        binding.expiryDateTextView.setTextColor(0xFFFF0000.toInt())
                    }
                    ExpiryAlertColor.ORANGE -> {
                        binding.expiryStatusTextView.setTextColor(0xFFFF8800.toInt())
                        binding.expiryDateTextView.setTextColor(0xFFFF8800.toInt())
                    }
                    ExpiryAlertColor.YELLOW -> {
                        binding.expiryStatusTextView.setTextColor(0xFFFFFF00.toInt())
                        binding.expiryDateTextView.setTextColor(0xFFFFFF00.toInt())
                    }
                    else -> {
                        binding.expiryStatusTextView.setTextColor(0xFF000000.toInt()) // Default black
                        binding.expiryDateTextView.setTextColor(0xFF000000.toInt()) // Default black
                    }
                }
                // Always hide the indicator line
                binding.expiryIndicator.visibility = View.GONE
            } else {
                binding.expiryStatusTextView.setTextColor(0xFF000000.toInt()) // Default black
                binding.expiryDateTextView.setTextColor(0xFF000000.toInt()) // Default black
                binding.expiryIndicator.visibility = View.GONE
            }

            // Set button visibility
            if (item.status == ItemStatus.ACTIVE) {
                binding.editButton.visibility = View.VISIBLE
                binding.deleteButton.setImageResource(android.R.drawable.ic_menu_delete)
            } else {
                binding.editButton.visibility = View.GONE
                binding.deleteButton.setImageResource(android.R.drawable.ic_menu_delete)
            }

            binding.editButton.setOnClickListener { onEditClicked(item) }
            binding.deleteButton.setOnClickListener { onDeleteClicked(item) }
        }
    }
}