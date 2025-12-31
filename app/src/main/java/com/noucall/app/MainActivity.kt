package com.noucall.app

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.noucall.app.adapter.CountryAutoCompleteAdapter
import com.noucall.app.adapter.PrefixAdapter
import com.noucall.app.adapter.WhitelistAdapter
import com.noucall.app.data.BlockedPrefix
import com.noucall.app.data.Country
import com.noucall.app.data.CountryData
import com.noucall.app.databinding.ActivityMainBinding
import com.noucall.app.receiver.CallBlockerReceiver
import com.noucall.app.utils.Constants
import com.noucall.app.utils.PermissionManager
import com.noucall.app.utils.SharedPreferencesManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var prefixAdapter: PrefixAdapter
    private lateinit var whitelistAdapter: WhitelistAdapter
    private var isBlockingEnabled = false
    private val callBlockerReceiver = CallBlockerReceiver()

    // Permission launchers
    private val callPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            checkAndRequestReadPhoneStatePermission()
        } else {
            Toast.makeText(this, getString(R.string.permission_call_phone), Toast.LENGTH_LONG).show()
        }
    }

    private val readPhoneStatePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            checkAndRequestReadCallLogPermission()
        } else {
            Toast.makeText(this, "Permission requise pour lire l'état du téléphone", Toast.LENGTH_LONG).show()
        }
    }

    private val readCallLogPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            checkAndRequestAnswerCallPermission()
        } else {
            Toast.makeText(this, "Permission requise pour lire le journal d'appels", Toast.LENGTH_LONG).show()
        }
    }

    private val answerCallPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            checkAndRequestSmsPermission()
        } else {
            Toast.makeText(this, "Permission requise pour bloquer les appels", Toast.LENGTH_LONG).show()
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
            Log.d("MainActivity", "Switch toggled. isChecked: $isChecked")
            // Save the state immediately
            sharedPreferences.edit().putBoolean(Constants.KEY_BLOCKING_ENABLED, isChecked).commit()
            
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
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED -> {
                readPhoneStatePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
            }
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED -> {
                readCallLogPermissionLauncher.launch(Manifest.permission.READ_CALL_LOG)
            }
            ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED -> {
                answerCallPermissionLauncher.launch(Manifest.permission.ANSWER_PHONE_CALLS)
            }
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED -> {
                smsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
            }
            !isDefaultDialer() -> {
                requestDefaultDialer()
                return
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

    private fun checkAndRequestReadPhoneStatePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            checkAndRequestReadCallLogPermission()
        } else {
            readPhoneStatePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
        }
    }

    private fun checkAndRequestReadCallLogPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            checkAndRequestAnswerCallPermission()
        } else {
            readCallLogPermissionLauncher.launch(Manifest.permission.READ_CALL_LOG)
        }
    }

    private fun checkAndRequestAnswerCallPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
            checkAndRequestSmsPermission()
        } else {
            answerCallPermissionLauncher.launch(Manifest.permission.ANSWER_PHONE_CALLS)
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

        // Register the call blocker receiver
        val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        registerReceiver(callBlockerReceiver, filter)

        Toast.makeText(this, getString(R.string.blocking_enabled), Toast.LENGTH_SHORT).show()
    }

    private fun disableBlocking() {
        isBlockingEnabled = false
        SharedPreferencesManager.setBlockingEnabled(this, false)
        binding.switchBlocking.isChecked = false

        // Unregister the call blocker receiver
        try {
            unregisterReceiver(callBlockerReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }

        Toast.makeText(this, getString(R.string.blocking_disabled), Toast.LENGTH_SHORT).show()
    }

    private fun checkBlockingStatus() {
        isBlockingEnabled = SharedPreferencesManager.isBlockingEnabled(this)
        binding.switchBlocking.isChecked = isBlockingEnabled

        // Register receiver if blocking is enabled
        if (isBlockingEnabled) {
            val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
            registerReceiver(callBlockerReceiver, filter)
        }
    }

    private fun loadBlockedPrefixes() {
        val prefixes = SharedPreferencesManager.getBlockedPrefixes(this)
        prefixAdapter.submitList(prefixes)
    }

    private fun loadWhitelistedCountries() {
        val countries = SharedPreferencesManager.getWhitelistedCountries(this)
        whitelistAdapter.submitList(countries.toMutableList())
    }

    private fun showAddChoiceDialog() {
        val options = arrayOf(getString(R.string.add_prefix), getString(R.string.title_whitelist))
        androidx.appcompat.app.AlertDialog.Builder(this, R.style.Theme_NoUCall_Dialog)
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
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val prefixEditText = EditText(this).apply {
            hint = getString(R.string.prefix_hint)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val commentEditText = EditText(this).apply {
            hint = "Commentaire (optionnel)"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        container.addView(prefixEditText)
        container.addView(commentEditText)

        androidx.appcompat.app.AlertDialog.Builder(this, R.style.Theme_NoUCall_Dialog)
            .setTitle(R.string.add_prefix)
            .setView(container)
            .setPositiveButton(R.string.save) { _, _ ->
                val prefix = prefixEditText.text.toString().trim()
                val comment = commentEditText.text.toString().trim()
                if (prefix.isNotEmpty()) {
                    SharedPreferencesManager.addBlockedPrefix(this, prefix, comment)
                    val updated = SharedPreferencesManager.getBlockedPrefixes(this)
                    prefixAdapter.submitList(updated)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showEditPrefixDialog(blockedPrefix: BlockedPrefix) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val prefixEditText = EditText(this).apply {
            setText(blockedPrefix.prefix)
            setSelection(text.length)
            hint = getString(R.string.prefix_hint)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val commentEditText = EditText(this).apply {
            setText(blockedPrefix.comment)
            setSelection(text.length)
            hint = "Commentaire"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        container.addView(prefixEditText)
        container.addView(commentEditText)

        androidx.appcompat.app.AlertDialog.Builder(this, R.style.Theme_NoUCall_Dialog)
            .setTitle(R.string.edit)
            .setView(container)
            .setPositiveButton(R.string.save) { _, _ ->
                val newPrefix = prefixEditText.text.toString().trim()
                val newComment = commentEditText.text.toString().trim()
                if (newPrefix.isNotEmpty()) {
                    val current = SharedPreferencesManager.getBlockedPrefixes(this).toMutableList()
                    val index = current.indexOfFirst { it.prefix == blockedPrefix.prefix }
                    if (index >= 0) {
                        current[index] = BlockedPrefix(newPrefix, newComment)
                        SharedPreferencesManager.getInstance(this).setBlockedPrefixes(current)
                        prefixAdapter.submitList(current)
                    }
                }
            }
            .setNegativeButton(R.string.delete) { _, _ ->
                SharedPreferencesManager.removeBlockedPrefix(this, blockedPrefix.prefix)
                val updated = SharedPreferencesManager.getBlockedPrefixes(this)
                prefixAdapter.submitList(updated)
            }
            .setNeutralButton(R.string.cancel, null)
            .show()
    }

    private fun showAddCountryDialog() {
        val editText = AutoCompleteTextView(this).apply {
            hint = getString(R.string.country_hint)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Setup auto-completion
        val adapter = CountryAutoCompleteAdapter(this)
        editText.setAdapter(adapter)
        editText.threshold = 1 // Start showing suggestions after 1 character

        androidx.appcompat.app.AlertDialog.Builder(this, R.style.Theme_NoUCall_Dialog)
            .setTitle(R.string.title_whitelist)
            .setView(editText)
            .setPositiveButton(R.string.save) { _, _ ->
                val input = editText.text.toString().trim()
                if (input.isNotEmpty()) {
                    // Try to find country by name or prefix
                    var country: Country? = null
                    
                    // First try to find by exact name
                    country = CountryData.findCountryByName(input)
                    
                    // If not found, try by prefix
                    if (country == null && input.startsWith("+")) {
                        country = CountryData.findCountryByPrefix(input)
                    }
                    
                    // If still not found, try to search and take first result
                    if (country == null) {
                        val searchResults = CountryData.searchCountries(input)
                        if (searchResults.isNotEmpty()) {
                            country = searchResults[0]
                        }
                    }
                    
                    if (country != null) {
                        // Save as "prefix name" format
                        val countryDisplay = "${country.prefix} ${country.name}"
                        SharedPreferencesManager.addWhitelistedCountry(this, countryDisplay)
                        val updated = SharedPreferencesManager.getWhitelistedCountries(this).toMutableList()
                        whitelistAdapter.submitList(updated)
                    } else {
                        // If no country found, save as is (for custom entries)
                        SharedPreferencesManager.addWhitelistedCountry(this, input)
                        val updated = SharedPreferencesManager.getWhitelistedCountries(this).toMutableList()
                        whitelistAdapter.submitList(updated)
                    }
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

        androidx.appcompat.app.AlertDialog.Builder(this, R.style.Theme_NoUCall_Dialog)
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

    private fun isDefaultDialer(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
            val defaultDialer = telecomManager.defaultDialerPackage
            defaultDialer == packageName
        } else {
            true // On older versions, no need to be default dialer
        }
    }

    private fun requestDefaultDialer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
            }
            startActivity(intent)
            Toast.makeText(this, "Veuillez définir NoUCall comme application téléphonique par défaut", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(callBlockerReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }
}
