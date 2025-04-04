package com.ssrlab.audioguide.botanic

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.room.Room
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.ssrlab.audioguide.botanic.app.MainApplication
import com.ssrlab.audioguide.botanic.databinding.ActivityMainBinding
import com.ssrlab.audioguide.botanic.db.ExhibitDao
import com.ssrlab.audioguide.botanic.db.ExhibitDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var exhibitDao: ExhibitDao

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var navController: NavController

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private lateinit var mainApp: MainApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainApp = MainApplication()
        mainApp.setContext(this@MainActivity)

        loadPreferences()
        setUpElements()
        addGraphListener(navController)

        bottomNav = binding.mainBottomNav
        bottomNav.apply {
            inflateMenu(R.menu.bottom_menu)
            setupWithNavController(navController)
        }

        if (!MapboxNavigationApp.isSetup()) {
            MapboxNavigationApp.setup {
                NavigationOptions.Builder(this@MainActivity)
                    .accessToken(resources.getString(R.string.mapbox_access_token))
                    .build()
            }
        }

        setTransparentStatusBar()
    }

    override fun onResume() {
        super.onResume()

        MapboxNavigationApp.attach(this)
    }

    override fun onPause() {
        super.onPause()

        MapboxNavigationApp.detach(this)
    }

    private fun setUpElements() {
        val db =
            Room.databaseBuilder(applicationContext, ExhibitDatabase::class.java, "exhibit_table")
                .fallbackToDestructiveMigration()
                .build()
        exhibitDao = db.exhibitDao()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    @Suppress("DEPRECATION")
    private fun loadPreferences() {
        val sharedPreferences = getSharedPreferences(mainApp.constPreferences, MODE_PRIVATE)
        val locale = sharedPreferences.getString(mainApp.constLocale, "ru")
        locale?.let { Locale(it) }?.let { mainApp.setLocale(it) }

        val config = mainApp.getContext().resources.configuration
        config.setLocale(Locale(locale!!))
        Locale.setDefault(Locale(locale))

        mainApp.getContext().resources.updateConfiguration(config, resources.displayMetrics)

        mainApp.setLocaleInt(locale)
    }

    fun savePreferences(locale: String) {
        val sharedPreferences =
            getSharedPreferences(mainApp.constPreferences, MODE_PRIVATE) ?: return
        with(sharedPreferences.edit()) {
            putString(mainApp.constLocale, locale)
            apply()
        }

        recreate()
    }

    private fun setTransparentStatusBar() {
        val window = window
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    private fun addGraphListener(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.fragmentMap) binding.mainBottomNav.visibility = View.GONE
            else binding.mainBottomNav.visibility = View.VISIBLE
        }
    }

    fun intentToDialer(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNumber")
        startActivity(intent)
    }

    fun intentToBrowser(address: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(address)
        startActivity(intent)
    }

    fun intentToMail(mail: String) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:$mail")
        startActivity(intent)
    }

    fun getNavController() = navController
    fun getApp() = mainApp
    fun getScope() = scope
    fun getDao() = exhibitDao
}