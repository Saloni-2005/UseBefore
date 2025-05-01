package com.example.usebefore.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.usebefore.repository.ItemStatus
import com.example.usebefore.view.ItemsFragment

class InventoryPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ItemsFragment.newInstance(ItemStatus.ACTIVE)
            1 -> ItemsFragment.newInstance(ItemStatus.USED)
            2 -> ItemsFragment.newInstance(ItemStatus.EXPIRED)
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}