package com.shinhan.ble

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shinhan.ble.data.ScannedUser

class ScannedUserAdapter(
    private val users: MutableList<ScannedUser>,
    private val onUserClick: (ScannedUser) -> Unit
) : RecyclerView.Adapter<ScannedUserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scanned_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        android.util.Log.d("ScannedUserAdapter", "onBindViewHolder called for position $position")
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateUsers(newUsers: List<ScannedUser>) {
        android.util.Log.d("ScannedUserAdapter", "updateUsers called with ${newUsers.size} users")
        for ((index, user) in newUsers.withIndex()) {
            android.util.Log.d("ScannedUserAdapter", "User $index: ${user.deviceName} (${user.deviceAddress})")
        }
        
        users.clear()
//        android.util.Log.d("ScannedUserAdapter", "Cleared users, size now: ${users.size}")
        
        users.addAll(newUsers)
//        android.util.Log.d("ScannedUserAdapter", "Added ${newUsers.size} users, adapter now has ${users.size} users")
        
        // Double check the contents
        for ((index, user) in users.withIndex()) {
            android.util.Log.d("ScannedUserAdapter", "Final User $index: ${user.deviceName} (${user.deviceAddress})")
        }
        
        notifyDataSetChanged()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorIndicator: View = itemView.findViewById(R.id.colorIndicator)
        private val userName: TextView = itemView.findViewById(R.id.userName)
        private val deviceAddress: TextView = itemView.findViewById(R.id.deviceAddress)
        private val rssiValue: TextView = itemView.findViewById(R.id.rssiValue)
        private val signalStrength: ImageView = itemView.findViewById(R.id.signalStrength)

        fun bind(user: ScannedUser) {
            android.util.Log.d("ScannedUserAdapter", "Binding user: ${user.deviceName} (${user.deviceAddress})")
            
            // Set color indicator
            val background = colorIndicator.background as GradientDrawable
            background.setColor(user.color)

            // Set user info
            userName.text = user.deviceName
            deviceAddress.text = user.deviceAddress
            rssiValue.text = "${user.rssi} dBm"

            // Set signal strength icon based on RSSI
            val signalIcon = when (ScannedUser.getProximityLevel(user.rssi)) {
                0 -> R.drawable.ic_signal_strength_4 // Very close
                1 -> R.drawable.ic_signal_strength_3 // Close
                2 -> R.drawable.ic_signal_strength_2 // Medium
                else -> R.drawable.ic_signal_strength_1 // Far
            }
            
            try {
                signalStrength.setImageResource(signalIcon)
            } catch (e: Exception) {
                // Fallback to default signal icon
                signalStrength.setImageResource(R.drawable.ic_signal_strength)
            }

            // Set click listener
            itemView.setOnClickListener {
                onUserClick(user)
            }
        }
    }
}