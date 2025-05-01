package com.example.usebefore.repository

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.util.concurrent.TimeUnit

enum class ItemStatus {
    ACTIVE,    // Item is active and not expired
    USED,      // Item was used/consumed before expiration
    EXPIRED    // Item has expired
}

@IgnoreExtraProperties
data class InventoryItem(
    val id: String = "",
    val name: String = "",
    val expiryDate: String = "",
    val expiryTimestamp: Long = 0,
    var status: ItemStatus = ItemStatus.ACTIVE,
    val createdAt: Long = 0
) {
    @Exclude
    fun isExpired(): Boolean {
        return System.currentTimeMillis() > expiryTimestamp
    }

    @Exclude
    fun daysUntilExpiry(): Int {
        val diffMillis = expiryTimestamp - System.currentTimeMillis()
        return TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
    }

    @Exclude
    fun getExpiryAlertColor(): ExpiryAlertColor {
        val daysLeft = daysUntilExpiry()
        return when {
            daysLeft <= 1 -> ExpiryAlertColor.RED        // 24 hours or less
            daysLeft <= 3 -> ExpiryAlertColor.ORANGE     // 3 days or less
            daysLeft <= 5 -> ExpiryAlertColor.YELLOW     // 5 days or less
            else -> ExpiryAlertColor.NONE
        }
    }
}

enum class ExpiryAlertColor {
    NONE,
    YELLOW,  // 5 days until expiry
    ORANGE,  // 3 days until expiry
    RED      // 24 hours until expiry
}