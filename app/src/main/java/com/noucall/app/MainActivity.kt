package com.noucall.app

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.noucall.app.adapter.PrefixAdapter
import com.noucall.app.adapter.WhitelistAdapter
import com.noucall.app.databinding.ActivityMainBinding
import com.noucall.app.utils.Constants
import com.noucall.app.utils.PermissionManager
import com.noucall.app.utils.SharedPreferencesManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var prefixAdapter: PrefixAdapter
    private lateinit var whitelistAdapter: WhitelistAdapter
    private var isBlockingEnabled = false

    // Permission launchers
    private val callPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            checkAndRequestSmsPermission()
        } else {
            Toast.makeText(this, getString(R.string.permission_call_phone), Toast.LENGTH_LONG).show()
        }
    }

    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            checkAndRequestOverlayPermission()
        } else {
            Toast.makeText(this, getString(R.string.permission_receive_sms), Toast.LENGTH_LONG).show()
        }
    }

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Settings.canDrawOverlays(this)) {
            enableBlocking()
        } else {
            Toast.makeText(this, getString(R.string.permission_system_alert_window), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply theme before setting content view
        applyTheme()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        
        setupUI()
        setupRecyclerViews()
        loadBlockedPrefixes()
        loadWhitelistedCountries()
        checkBlockingStatus()
    }

    private fun applyTheme() {
        val isDarkMode = SharedPreferencesManager.isDarkMode(this)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        
        binding.fabAddCountry.setOnClickListener {
            showAddChoiceDialog()
        }
        
        binding.switchBlocking.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkAndRequestPermissions()
            } else {
                disableBlocking()
            }
        }
    }

    private fun setupRecyclerViews() {
        // Setup blocked prefixes RecyclerView
        prefixAdapter = PrefixAdapter { prefix ->
            showEditPrefixDialog(prefix)
        }
        binding.recyclerViewBlockedPrefixes.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = prefixAdapter
        }
        
        // Setup whitelist RecyclerView
        whitelistAdapter = WhitelistAdapter { country ->
            showEditCountryDialog(country)
        }
        binding.recyclerViewWhitelistedCountries.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = whitelistAdapter
        }
    }

    private fun checkAndRequestPermissions() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED -> {
                callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
            }
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED -> {
                smsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
            }
            !Settings.canDrawOverlays(this) -> {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
                overlayPermissionLauncher.launch(intent)
            }
            else -> {
                enableBlocking()
            }
        }
    }

    private fun checkAndRequestCallPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            checkAndRequestSmsPermission()
        } else {
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    private fun checkAndRequestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) {
            checkAndRequestOverlayPermission()
        } else {
            smsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
        }
    }

    private fun checkAndRequestOverlayPermission() {
        if (Settings.canDrawOverlays(this)) {
            enableBlocking()
        } else {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:$packageName")
            }
            overlayPermissionLauncher.launch(intent)
        }
    }

    private fun enableBlocking() {
        isBlockingEnabled = true
        SharedPreferencesManager.setBlockingEnabled(this, true)
        binding.switchBlocking.isChecked = true
        Toast.makeText(this, getString(R.string.blocking_enabled), Toast.LENGTH_SHORT).show()
    }

    private fun disableBlocking() {
        isBlockingEnabled = false
        SharedPreferencesManager.setBlockingEnabled(this, false)
        binding.switchBlocking.isChecked = false
        Toast.makeText(this, getString(R.string.blocking_disabled), Toast.LENGTH_SHORT).show()
    }

    private fun checkBlockingStatus() {
        isBlockingEnabled = SharedPreferencesManager.isBlockingEnabled(this)
        binding.switchBlocking.isChecked = isBlockingEnabled
    }

    private fun loadBlockedPrefixes() {
        val prefixes = SharedPreferencesManager.getBlockedPrefixes(this)
        prefixAdapter.submitList(prefixes.toMutableList())
    }

    private fun loadWhitelistedCountries() {
        val countries = SharedPreferencesManager.getWhitelistedCountries(this)
        whitelistAdapter.submitList(countries.toMutableList())
    }

    private fun showAddChoiceDialog() {
        val options = arrayOf(getString(R.string.add_prefix), getString(R.string.title_whitelist))
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Ajouter")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showAddPrefixDialog()
                    1 -> showAddCountryDialog()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showAddPrefixDialog() {
        val editText = EditText(this).apply {
            hint = getString(R.string.prefix_hint)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.add_prefix)
            .setView(editText)
            .setPositiveButton(R.string.save) { _, _ ->
                val value = editText.text.toString().trim()
                if (value.isNotEmpty()) {
                    SharedPreferencesManager.addBlockedPrefix(this, value)
                    val updated = SharedPreferencesManager.getBlockedPrefixes(this).toMutableList()
                    prefixAdapter.submitList(updated)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showEditPrefixDialog(prefix: String) {
        val editText = EditText(this).apply {
            setText(prefix)
            setSelection(text.length)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.edit)
            .setView(editText)
            .setPositiveButton(R.string.save) { _, _ ->
                val newValue = editText.text.toString().trim()
                if (newValue.isNotEmpty()) {
                    val current = SharedPreferencesManager.getBlockedPrefixes(this).toMutableList()
                    val index = current.indexOf(prefix)
                    if (index >= 0) {
                        current[index] = newValue
                        SharedPreferencesManager.getInstance(this).setBlockedPrefixes(current)
                        prefixAdapter.submitList(current.toMutableList())
                    }
                }
            }
            .setNegativeButton(R.string.delete) { _, _ ->
                SharedPreferencesManager.removeBlockedPrefix(this, prefix)
                val updated = SharedPreferencesManager.getBlockedPrefixes(this).toMutableList()
                prefixAdapter.submitList(updated)
            }
            .setNeutralButton(R.string.cancel, null)
            .show()
    }

    private fun showAddCountryDialog() {
        val editText = EditText(this).apply {
            hint = getString(R.string.country_hint)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.title_whitelist)
            .setView(editText)
            .setPositiveButton(R.string.save) { _, _ ->
                val value = editText.text.toString().trim()
                if (value.isNotEmpty()) {
                    SharedPreferencesManager.addWhitelistedCountry(this, value)
                    val updated = SharedPreferencesManager.getWhitelistedCountries(this).toMutableList()
                    whitelistAdapter.submitList(updated)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showEditCountryDialog(country: String) {
        val editText = EditText(this).apply {
            setText(country)
            setSelection(text.length)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.edit)
            .setView(editText)
            .setPositiveButton(R.string.save) { _, _ ->
                val newValue = editText.text.toString().trim()
                if (newValue.isNotEmpty()) {
                    val current = SharedPreferencesManager.getWhitelistedCountries(this).toMutableList()
                    val index = current.indexOf(country)
                    if (index >= 0) {
                        current[index] = newValue
                        SharedPreferencesManager.getInstance(this).setWhitelistedCountries(current)
                        whitelistAdapter.submitList(current.toMutableList())
                    }
                }
            }
            .setNegativeButton(R.string.delete) { _, _ ->
                SharedPreferencesManager.removeWhitelistedCountry(this, country)
                val updated = SharedPreferencesManager.getWhitelistedCountries(this).toMutableList()
                whitelistAdapter.submitList(updated)
            }
            .setNeutralButton(R.string.cancel, null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_statistics -> {
                startActivity(Intent(this, StatisticsActivity::class.java))
                true
            }
            R.id.action_theme -> {
                toggleTheme()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleTheme() {
        val isDarkMode = !SharedPreferencesManager.isDarkMode(this)
        SharedPreferencesManager.setDarkMode(this, isDarkMode)
        recreate()
    }
}
