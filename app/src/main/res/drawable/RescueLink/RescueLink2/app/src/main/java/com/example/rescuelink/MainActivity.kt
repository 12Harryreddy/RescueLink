package com.example.rescuelink


import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val alertsFragment = AlertsFragment()
    private val sosFragment = SOSFragment()
    private val mapFragment = RescueMapFragment()
    private val contactsFragment = ContactsFragment()
    private val settingsFragment = SettingsFragment()

    override fun attachBaseContext(newBase: Context) {
        val langCode = newBase.getSharedPreferences("settings_pref", Context.MODE_PRIVATE)
            .getString("app_language", "en") ?: "en"
        val context = newBase.updateLocale(langCode)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            loadFragment(sosFragment)
        }

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_alerts -> loadFragment(alertsFragment)
                R.id.nav_sos -> loadFragment(sosFragment)
                R.id.nav_map -> loadFragment(mapFragment)
                R.id.nav_contacts -> loadFragment(contactsFragment)
                R.id.nav_settings -> loadFragment(settingsFragment)
            }
            true
        }
    }

    // Update the app's locale based on the selected language
    fun Context.updateLocale(language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        return createConfigurationContext(config)
    }

    // Load fragment and avoid replacing if the same fragment is selected
    private fun loadFragment(fragment: Fragment) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        // Only replace the fragment if it's not the same one already displayed
        if (currentFragment !is Fragment || currentFragment::class != fragment::class) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss()  // Commit without throwing an exception if state is lost
        }
    }
}
