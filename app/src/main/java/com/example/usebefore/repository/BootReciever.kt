package com.example.usebefore.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.usebefore.repository.InventoryItem
import com.example.usebefore.repository.ItemStatus
import com.example.usebefore.viewmodel.PreferenceHelper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val preferenceHelper = PreferenceHelper(context)

            if (preferenceHelper.isNotificationsEnabled()) {
                rescheduleAllNotifications(context)
            }
        }
    }

    private fun rescheduleAllNotifications(context: Context) {
        val notificationManager = NotificationManager(context)
        val database = FirebaseDatabase.getInstance().reference.child("inventory")

        database.orderByChild("status").equalTo(ItemStatus.ACTIVE.name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val items = mutableListOf<InventoryItem>()

                    for (itemSnapshot in snapshot.children) {
                        val item = itemSnapshot.getValue(InventoryItem::class.java)
                        item?.let { items.add(it) }
                    }

                    for (item in items) {
                        notificationManager.scheduleNotificationsForItem(item)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }
}