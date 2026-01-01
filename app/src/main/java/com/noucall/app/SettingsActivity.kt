package com.noucall.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.noucall.app.utils.SharedPreferencesManager

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var switchDarkMode: Switch
    private lateinit var tvLastDetectedNumber: TextView
    private lateinit var tvLastDetectionReason: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply theme before setting content view
        applyTheme()
        
        setContentView(R.layout.activity_settings)
        
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        initViews()
        loadSettings()
    }
    
    private fun initViews() {
        switchDarkMode = findViewById(R.id.switch_dark_mode)
        tvLastDetectedNumber = findViewById(R.id.tv_last_detected_number)
        tvLastDetectionReason = findViewById(R.id.tv_last_detection_reason)
        
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            SharedPreferencesManager.setDarkMode(this, isChecked)
            applyTheme()
            // Recreate activity to apply theme
            recreate()
        }
    }
    
    private fun loadSettings() {
        // Load dark mode setting
        val isDarkMode = SharedPreferencesManager.isDarkMode(this)
        switchDarkMode.isChecked = isDarkMode
        
        // Load last detected number info
        val lastNumber = SharedPreferencesManager.getLastDetectedNumber(this)
        val lastReason = SharedPreferencesManager.getLastDetectionReason(this)
        val lastTimestamp = SharedPreferencesManager.getLastDetectionTimestamp(this)
        
        if (lastNumber.isNotEmpty()) {
            tvLastDetectedNumber.text = "Numéro: $lastNumber"
            tvLastDetectionReason.text = "Raison: $lastReason\nHeure: ${formatTimestamp(lastTimestamp)}"
        } else {
            tvLastDetectedNumber.text = "Aucun numéro détecté"
            tvLastDetectionReason.text = ""
        }
    }
    
    private fun applyTheme() {
        val isDarkMode = SharedPreferencesManager.isDarkMode(this)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
    
    private fun formatTimestamp(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_clear_logs -> {
                clearLastDetection()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun clearLastDetection() {
        SharedPreferencesManager.clearLastDetection(this)
        loadSettings()
        android.widget.Toast.makeText(this, "Dernière détection effacée", android.widget.Toast.LENGTH_SHORT).show()
    }
}
