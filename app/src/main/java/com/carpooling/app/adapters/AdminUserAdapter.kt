package com.carpooling.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.carpooling.app.R
import com.carpooling.app.databinding.ItemAdminUserBinding
import com.carpooling.app.models.User

/**
 * Adapter for displaying users in admin manage users view.
 */
class AdminUserAdapter(
    private var users: List<User>,
    private val onToggleBanClick: (User) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.UserViewHolder>() {
    
    inner class UserViewHolder(private val binding: ItemAdminUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(user: User) {
            // Role badge
            binding.tvRole.text = user.role
            val roleColor = when (user.role) {
                "DRIVER" -> ContextCompat.getColor(itemView.context, R.color.primary)
                "PASSENGER" -> ContextCompat.getColor(itemView.context, R.color.status_success)
                else -> ContextCompat.getColor(itemView.context, R.color.text_secondary)
            }
            binding.tvRole.background.setTint(roleColor)
            
            // Ban status
            if (user.isBanned) {
                binding.tvBanStatus.visibility = View.VISIBLE
                binding.tvBanStatus.text = itemView.context.getString(R.string.admin_banned)
            } else {
                binding.tvBanStatus.visibility = View.GONE
            }
            
            // User info
            binding.tvEmail.text = user.email
            binding.tvPhone.text = "Phone: ${user.phoneNumber}"
            binding.tvGender.text = "Gender: ${user.gender}"
            
            // Toggle ban button
            if (user.isBanned) {
                binding.btnToggleBan.text = itemView.context.getString(R.string.admin_unban_user)
                binding.btnToggleBan.backgroundTintList = 
                    ContextCompat.getColorStateList(itemView.context, R.color.status_success)
            } else {
                binding.btnToggleBan.text = itemView.context.getString(R.string.admin_ban_user)
                binding.btnToggleBan.backgroundTintList = 
                    ContextCompat.getColorStateList(itemView.context, R.color.status_danger)
            }
            
            binding.btnToggleBan.setOnClickListener {
                onToggleBanClick(user)
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemAdminUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }
    
    override fun getItemCount() = users.size
    
    fun updateUsers(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
