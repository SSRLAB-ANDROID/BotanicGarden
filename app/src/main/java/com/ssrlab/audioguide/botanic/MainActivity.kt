package com.ssrlab.audioguide.botanic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.room.Room
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ssrlab.audioguide.botanic.app.MainApplication
import com.ssrlab.audioguide.botanic.databinding.ActivityMainBinding
import com.ssrlab.audioguide.botanic.db.ExhibitDao
import com.ssrlab.audioguide.botanic.db.ExhibitDatabase
import com.ssrlab.audioguide.botanic.vm.ExhibitViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var exhibitDao: ExhibitDao

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var navController: NavController

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val viewModel: ExhibitViewModel by viewModels()
    private lateinit var mainApp: MainApplication

    private lateinit var audioManager: AudioManager
    private val volumeChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "android.media.VOLUME_CHANGED_ACTION") {

                val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                viewModel.isVolumeOn.value = currentVolume != 0
            }
        }
    }

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

        setUpVolumeStateListener()
        setTransparentStatusBar()
    }

    override fun onResume() {
        super.onResume()

        registerReceiver(volumeChangeReceiver, IntentFilter("android.media.VOLUME_CHANGED_ACTION"))
    }

    override fun onPause() {
        super.onPause()

        unregisterReceiver(volumeChangeReceiver)
    }

    private fun setUpElements() {
        val db = Room.databaseBuilder(applicationContext, ExhibitDatabase::class.java, "exhibit_table")
            .fallbackToDestructiveMigration()
            .build()
        exhibitDao = db.exhibitDao()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun setUpVolumeStateListener() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        viewModel.isVolumeOn.value = currentVolume != 0
    }

    @Suppress("DEPRECATION")
    private fun loadPreferences(){
        val sharedPreferences = getSharedPreferences(mainApp.constPreferences, MODE_PRIVATE)
        val locale = sharedPreferences.getString(mainApp.constLocale, "en")
        locale?.let { Locale(it) }?.let { mainApp.setLocale(it) }

        val config = mainApp.getContext().resources.configuration
        config.setLocale(Locale(locale!!))
        Locale.setDefault(Locale(locale))

        mainApp.getContext().resources.updateConfiguration(config, resources.displayMetrics)

        mainApp.setLocaleInt(locale)
    }

    fun savePreferences(locale: String){
        val sharedPreferences = getSharedPreferences(mainApp.constPreferences, MODE_PRIVATE) ?: return
        with (sharedPreferences.edit()){
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
        navController.addOnDestinationChangedListener{ _, destination, _ ->
            if (destination.id == R.id.fragmentMap) binding.mainBottomNav.visibility = View.GONE
            else binding.mainBottomNav.visibility = View.VISIBLE
        }
    }

    fun controlVolume(value: Int) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0)
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

    fun getApp() = mainApp
    fun getScope() = scope
    fun getDao() = exhibitDao
}