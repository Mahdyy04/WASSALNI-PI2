package com.carpooling.app.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.carpooling.app.R
import com.carpooling.app.adapters.AdminReportAdapter
import com.carpooling.app.databinding.FragmentAdminManageReportsBinding
import com.carpooling.app.models.Report
import com.carpooling.app.network.RetrofitClient
import kotlinx.coroutines.launch

/**
 * Admin fragment to manage reports and ban users.
 */
class AdminManageReportsFragment : Fragment() {
    
    private var _binding: FragmentAdminManageReportsBinding? = null
    private val binding get() = _binding!!
    private lateinit var reportAdapter: AdminReportAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminManageReportsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadReports()
        
        binding.swipeRefresh.setOnRefreshListener {
            loadReports()
        }
    }
    
    private fun setupRecyclerView() {
        reportAdapter = AdminReportAdapter(
            reports = emptyList(),
            onBanClick = { report ->
                showBanConfirmationDialog(report)
            },
            onDismissClick = { report ->
                dismissReport(report)
            }
        )
        binding.rvReports.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reportAdapter
        }
    }
    
    private fun loadReports() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getAllReports()
                if (response.isSuccessful && response.body() != null) {
                    val reports = response.body()!!
                    // Filter to show only pending reports first, then others
                    val sortedReports = reports.sortedByDescending { it.status == "PENDING" }
                    
                    if (sortedReports.isEmpty()) {
                        showEmptyState()
                    } else {
                        val pendingCount = sortedReports.count { it.status == "PENDING" }
                        binding.tvReportCount.text = "Pending reports: $pendingCount / Total: ${sortedReports.size}"
                        reportAdapter.updateReports(sortedReports)
                        binding.rvReports.visibility = View.VISIBLE
                        binding.tvEmpty.visibility = View.GONE
                    }
                } else {
                    showEmptyState()
                    Toast.makeText(context, "Failed to load reports", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                showEmptyState()
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
    
    private fun showBanConfirmationDialog(report: Report) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.admin_ban_confirm_title)
            .setMessage(getString(R.string.admin_ban_confirm_message))
            .setPositiveButton(R.string.admin_ban_user) { _, _ ->
                banUserAndResolveReport(report)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun banUserAndResolveReport(report: Report) {
        lifecycleScope.launch {
            try {
                // Ban the reported user
                val banResponse = RetrofitClient.apiService.banUser(report.reportedUserId)
                if (banResponse.isSuccessful) {
                    // Update report status to RESOLVED
                    val statusResponse = RetrofitClient.apiService.updateReportStatus(report.id, "RESOLVED")
                    if (statusResponse.isSuccessful) {
                        Toast.makeText(context, getString(R.string.admin_user_banned), Toast.LENGTH_SHORT).show()
                        loadReports() // Refresh the list
                    } else {
                        Toast.makeText(context, "User banned but failed to update report status", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, getString(R.string.admin_ban_failed), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun dismissReport(report: Report) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.updateReportStatus(report.id, "DISMISSED")
                if (response.isSuccessful) {
                    Toast.makeText(context, getString(R.string.admin_report_dismissed), Toast.LENGTH_SHORT).show()
                    loadReports() // Refresh the list
                } else {
                    Toast.makeText(context, "Failed to dismiss report", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showEmptyState() {
        binding.tvEmpty.visibility = View.VISIBLE
        binding.rvReports.visibility = View.GONE
        binding.tvReportCount.text = "Pending reports: 0"
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
