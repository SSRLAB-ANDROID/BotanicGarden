package com.ssrlab.audioguide.botanic

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.room.Room
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ssrlab.audioguide.botanic.databinding.ActivityMainBinding
import com.ssrlab.audioguide.botanic.db.ExhibitDao
import com.ssrlab.audioguide.botanic.db.ExhibitDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var exhibitDao: ExhibitDao

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var navController: NavController

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = Room.databaseBuilder(applicationContext, ExhibitDatabase::class.java, "exhibit_table")
            .fallbackToDestructiveMigration()
            .build()
        exhibitDao = db.exhibitDao()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        bottomNav = binding.mainBottomNav
        bottomNav.setupWithNavController(navController)

        setTransparentStatusBar()
    }

    private fun setTransparentStatusBar() {
        val window = window
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
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

    fun getScope() = scope
    fun getDao() = exhibitDao
}