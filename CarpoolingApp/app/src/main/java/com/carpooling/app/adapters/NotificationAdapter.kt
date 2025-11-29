package com.carpooling.app.adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.carpooling.app.R
import com.carpooling.app.models.Notification
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class NotificationAdapter(
    private val notifications: List<Notification>,
    private val onNotificationClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }
    
    override fun getItemCount(): Int = notifications.size
    
    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardNotification)
        private val iconView: ImageView = itemView.findViewById(R.id.ivNotificationIcon)
        private val titleView: TextView = itemView.findViewById(R.id.tvNotificationTitle)
        private val messageView: TextView = itemView.findViewById(R.id.tvNotificationMessage)
        private val timeView: TextView = itemView.findViewById(R.id.tvNotificationTime)
        private val unreadIndicator: View = itemView.findViewById(R.id.viewUnreadIndicator)
        
        fun bind(notification: Notification) {
            titleView.text = notification.title
            messageView.text = notification.message
            timeView.text = formatTime(notification.createdAt)
            
            // Set icon based on notification type
            val iconRes = when (notification.type) {
                "BOOKING_REQUEST" -> R.drawable.ic_notification_request
                "BOOKING_ACCEPTED" -> R.drawable.ic_notification_accepted
                "BOOKING_REJECTED" -> R.drawable.ic_notification_rejected
                else -> R.drawable.ic_notification_default
            }
            iconView.setImageResource(iconRes)
            
            // Style for read/unread
            if (notification.isRead) {
                unreadIndicator.visibility = View.GONE
                titleView.setTypeface(null, Typeface.NORMAL)
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.context, android.R.color.white)
                )
            } else {
                unreadIndicator.visibility = View.VISIBLE
                titleView.setTypeface(null, Typeface.BOLD)
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.notification_unread_bg)
                )
            }
            
            itemView.setOnClickListener {
                onNotificationClick(notification)
            }
        }
        
        private fun formatTime(createdAt: String): String {
            return try {
                val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                val dateTime = LocalDateTime.parse(createdAt, formatter)
                val now = LocalDateTime.now()
                
                val minutes = ChronoUnit.MINUTES.between(dateTime, now)
                val hours = ChronoUnit.HOURS.between(dateTime, now)
                val days = ChronoUnit.DAYS.between(dateTime, now)
                
                when {
                    minutes < 1 -> "Just now"
                    minutes < 60 -> "${minutes}m ago"
                    hours < 24 -> "${hours}h ago"
                    days < 7 -> "${days}d ago"
                    else -> dateTime.format(DateTimeFormatter.ofPattern("MMM d"))
                }
            } catch (e: Exception) {
                createdAt
            }
        }
    }
}
