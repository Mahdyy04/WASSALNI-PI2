package com.carpooling.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.carpooling.app.R
import com.carpooling.app.databinding.ItemAdminReportBinding
import com.carpooling.app.models.Report

/**
 * Adapter for displaying reports in admin manage reports view.
 */
class AdminReportAdapter(
    private var reports: List<Report>,
    private val onBanClick: (Report) -> Unit,
    private val onDismissClick: (Report) -> Unit
) : RecyclerView.Adapter<AdminReportAdapter.ReportViewHolder>() {
    
    companion object {
        private const val RIDE_ID_DISPLAY_LENGTH = 8
    }
    
    inner class ReportViewHolder(private val binding: ItemAdminReportBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(report: Report) {
            // Status badge
            binding.tvStatus.text = report.status
            val statusColor = when (report.status) {
                "PENDING" -> ContextCompat.getColor(itemView.context, R.color.status_warning)
                "RESOLVED" -> ContextCompat.getColor(itemView.context, R.color.status_success)
                "DISMISSED" -> ContextCompat.getColor(itemView.context, R.color.text_secondary)
                else -> ContextCompat.getColor(itemView.context, R.color.primary)
            }
            binding.tvStatus.background.setTint(statusColor)
            
            // Report reason with formatted display
            val reasonDisplay = report.reason.replace("_", " ")
                .lowercase()
                .replaceFirstChar { it.uppercase() }
            binding.tvReason.text = "Reason: $reasonDisplay"
            
            // Reported user (will show user ID, ideally fetch email in production)
            binding.tvReportedUser.text = "Reported User ID: ${report.reportedUserId.take(RIDE_ID_DISPLAY_LENGTH)}..."
            
            // Reporter (anonymous)
            binding.tvReporter.text = "Reporter: Anonymous"
            
            // Ride ID
            binding.tvRideId.text = "Ride: #${report.rideId.take(RIDE_ID_DISPLAY_LENGTH)}"
            
            // Description
            if (report.description.isNotEmpty()) {
                binding.tvDescription.text = report.description
                binding.tvDescription.visibility = View.VISIBLE
            } else {
                binding.tvDescription.visibility = View.GONE
            }
            
            // Action buttons - only show for PENDING reports
            if (report.status == "PENDING") {
                binding.btnBanUser.visibility = View.VISIBLE
                binding.btnDismiss.visibility = View.VISIBLE
                
                binding.btnBanUser.setOnClickListener {
                    onBanClick(report)
                }
                
                binding.btnDismiss.setOnClickListener {
                    onDismissClick(report)
                }
            } else {
                binding.btnBanUser.visibility = View.GONE
                binding.btnDismiss.visibility = View.GONE
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemAdminReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReportViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position])
    }
    
    override fun getItemCount() = reports.size
    
    fun updateReports(newReports: List<Report>) {
        reports = newReports
        notifyDataSetChanged()
    }
}
