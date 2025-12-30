package com.noucall.app

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.noucall.app.adapter.BlockedCallAdapter
import com.noucall.app.adapter.BlockedSmsAdapter
import com.noucall.app.databinding.ActivityStatisticsBinding
import com.noucall.app.utils.SharedPreferencesManager
import java.text.SimpleDateFormat
import java.util.*

class StatisticsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var blockedCallAdapter: BlockedCallAdapter
    private lateinit var blockedSmsAdapter: BlockedSmsAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply theme before setting content view
        applyTheme()
        
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupUI()
        loadStatistics()
    }

    override fun onResume() {
        super.onResume()
        loadStatistics() // Refresh data when returning to the activity
    }
    
    private fun applyTheme() {
        val isDarkMode = SharedPreferencesManager.isDarkMode(this)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
    
    private fun setupUI() {
        // Setup RecyclerViews
        blockedCallAdapter = BlockedCallAdapter()
        binding.recyclerViewBlockedCalls.apply {
            layoutManager = LinearLayoutManager(this@StatisticsActivity)
            adapter = blockedCallAdapter
        }
        
        blockedSmsAdapter = BlockedSmsAdapter()
        binding.recyclerViewBlockedSms.apply {
            layoutManager = LinearLayoutManager(this@StatisticsActivity)
            adapter = blockedSmsAdapter
        }
    }
    
    private fun loadStatistics() {
        val blockedCallsCount = SharedPreferencesManager.getBlockedCallsCount(this)
        val blockedSmsCount = SharedPreferencesManager.getBlockedSmsCount(this)

        binding.tvBlockedCalls.text = getString(R.string.blocked_calls, blockedCallsCount)
        binding.tvBlockedSms.text = getString(R.string.blocked_sms, blockedSmsCount)

        loadBlockedCallsHistory()
        loadBlockedSmsHistory()
    }
    
    private fun loadBlockedCallsHistory() {
        val blockedCalls = SharedPreferencesManager.getBlockedCallsHistory(this)
        
        if (blockedCalls.isEmpty()) {
            binding.tvNoCallsData.visibility = android.view.View.VISIBLE
            binding.recyclerViewBlockedCalls.visibility = android.view.View.GONE
        } else {
            binding.tvNoCallsData.visibility = android.view.View.GONE
            binding.recyclerViewBlockedCalls.visibility = android.view.View.VISIBLE
            blockedCallAdapter.submitList(blockedCalls)
        }
    }
    
    private fun loadBlockedSmsHistory() {
        val blockedSms = SharedPreferencesManager.getBlockedSmsHistory(this)
        
        if (blockedSms.isEmpty()) {
            binding.tvNoSmsData.visibility = android.view.View.VISIBLE
            binding.recyclerViewBlockedSms.visibility = android.view.View.GONE
        } else {
            binding.tvNoSmsData.visibility = android.view.View.GONE
            binding.recyclerViewBlockedSms.visibility = android.view.View.VISIBLE
            blockedSmsAdapter.submitList(blockedSms)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.statistics_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_clear_history -> {
                clearHistory()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun clearHistory() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Effacer l'historique")
            .setMessage("Voulez-vous effacer tout l'historique des appels et SMS bloqués ?")
            .setPositiveButton("Oui") { _, _ ->
                SharedPreferencesManager.clearHistory(this)
                loadStatistics()
                android.widget.Toast.makeText(this, "Historique effacé", android.widget.Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Non", null)
            .show()
    }
    
    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
