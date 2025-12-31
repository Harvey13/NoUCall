package com.noucall.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.noucall.app.adapter.BlockedCallAdapter
import com.noucall.app.databinding.ActivityStatisticsBinding
import com.noucall.app.utils.SharedPreferencesManager
import java.text.SimpleDateFormat
import java.util.*

class StatisticsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var blockedCallAdapter: BlockedCallAdapter
    
    private val statisticsUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.noucall.app.STATISTICS_UPDATED") {
                loadStatistics()
            }
        }
    }
    
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
        // Register for statistics updates
        registerReceiver(statisticsUpdateReceiver, IntentFilter("com.noucall.app.STATISTICS_UPDATED"))
    }
    
    override fun onPause() {
        super.onPause()
        // Unregister receiver to avoid leaks
        try {
            unregisterReceiver(statisticsUpdateReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
        }
    }
    
    private fun applyTheme() {
        val isDarkMode = SharedPreferencesManager.isDarkMode(this)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
    
    private fun setupUI() {
        // Setup RecyclerViews
        blockedCallAdapter = BlockedCallAdapter { blockedCall ->
            // Handle edit click for blocked call
            showEditDialog(blockedCall.phoneNumber, "Appel")
        }
        binding.recyclerViewBlockedCalls.apply {
            layoutManager = LinearLayoutManager(this@StatisticsActivity)
            adapter = blockedCallAdapter
        }
        
    }
    
    private fun loadStatistics() {
        val blockedCallsCount = SharedPreferencesManager.getBlockedCallsCount(this)

        binding.tvBlockedCalls.text = "Appels Bloqués: $blockedCallsCount"

        loadBlockedCallsHistory()
    }
    
    private fun loadBlockedCallsHistory() {
        val blockedCalls = SharedPreferencesManager.getBlockedCallsHistory(this)
        
        if (blockedCalls.isEmpty()) {
            binding.tvNoCallsData.visibility = android.view.View.VISIBLE
            binding.recyclerViewBlockedCalls.visibility = android.view.View.GONE
        } else {
            binding.tvNoCallsData.visibility = android.view.View.GONE
            binding.recyclerViewBlockedCalls.visibility = android.view.View.VISIBLE
            // Sort by timestamp descending (most recent first)
            val sortedCalls = blockedCalls.sortedByDescending { it.timestamp }
            blockedCallAdapter.submitList(sortedCalls)
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
        androidx.appcompat.app.AlertDialog.Builder(this, R.style.Theme_NoUCall_Dialog)
            .setTitle("Effacer l'historique")
            .setMessage("Voulez-vous effacer tout l'historique des appels bloqués ?")
            .setPositiveButton("Oui") { _, _ ->
                SharedPreferencesManager.clearHistory(this)
                loadStatistics()
                android.widget.Toast.makeText(this, "Historique effacé", android.widget.Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Non", null)
            .show()
    }
    
    private fun showEditDialog(phoneNumber: String, type: String, message: String? = null) {
        val message = message ?: ""
        val fullMessage = when (type) {
            "Appel" -> "Numéro : $phoneNumber\nType : Appel bloqué\n\nQue souhaitez-vous faire ?"
            else -> "Numéro : $phoneNumber\nType : $type\n\nQue souhaitez-vous faire ?"
        }
        
        androidx.appcompat.app.AlertDialog.Builder(this, R.style.Theme_NoUCall_Dialog)
            .setTitle("Options")
            .setMessage(fullMessage)
            .setPositiveButton("Ajouter aux préfixes bloqués") { _, _ ->
                // Extract prefix from phone number
                val prefix = extractPrefix(phoneNumber)
                androidx.appcompat.app.AlertDialog.Builder(this, R.style.Theme_NoUCall_Dialog)
                    .setTitle("Ajouter un préfixe")
                    .setMessage("Ajouter '$prefix' aux préfixes bloqués ?")
                    .setPositiveButton("Oui") { _, _ ->
                        SharedPreferencesManager.addBlockedPrefix(this, prefix, "Ajouté depuis l'historique")
                        android.widget.Toast.makeText(this, "Préfixe ajouté", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Non", null)
                    .show()
            }
            .setNegativeButton("Copier le numéro") { _, _ ->
                // Copy to clipboard
                val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Numéro", phoneNumber)
                clipboard.setPrimaryClip(clip)
                android.widget.Toast.makeText(this, "Numéro copié", android.widget.Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Fermer", null)
            .show()
    }
    
    private fun extractPrefix(phoneNumber: String): String {
        // Remove spaces, +, and other characters
        val cleanNumber = phoneNumber.replace("[^0-9]".toRegex(), "")
        
        // For French numbers, return first 2 digits after removing +33
        if (phoneNumber.startsWith("+33") || phoneNumber.startsWith("0033")) {
            val nationalNumber = if (phoneNumber.startsWith("+33")) {
                cleanNumber.substring(2) // Remove 33
            } else {
                cleanNumber.substring(4) // Remove 0033
            }
            return if (nationalNumber.startsWith("0")) {
                nationalNumber.substring(0, 2) // Return first 2 digits after 0
            } else {
                nationalNumber.substring(0, Math.min(2, nationalNumber.length))
            }
        }
        
        // For other countries, return first 2-3 digits
        return cleanNumber.substring(0, Math.min(3, cleanNumber.length))
    }
    
    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
