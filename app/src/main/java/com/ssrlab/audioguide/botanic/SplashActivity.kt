package com.ssrlab.audioguide.botanic

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.ssrlab.audioguide.botanic.app.MainApplication
import com.ssrlab.audioguide.botanic.client.ExhibitClient
import com.ssrlab.audioguide.botanic.databinding.ActivitySplashBinding
import com.ssrlab.audioguide.botanic.db.ExhibitDao
import com.ssrlab.audioguide.botanic.db.ExhibitDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.*

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var exhibitDao: ExhibitDao
    private lateinit var mainApp: MainApplication

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainApp = MainApplication()
        mainApp.setContext(this@SplashActivity)

        loadPreferences()

        binding.apply {
            startGuide.text = resources.getText(R.string.audio_guide)
            startName.text = resources.getText(R.string.botanic_garden)
            startButton.text = resources.getText(R.string.start)
        }

        val db = Room.databaseBuilder(applicationContext, ExhibitDatabase::class.java, "exhibit_table")
            .fallbackToDestructiveMigration()
            .build()
        exhibitDao = db.exhibitDao()

        ExhibitClient.getExhibits(scope, exhibitDao, {
            runOnUiThread {
                binding.apply{
                    startButton.setOnClickListener { navigateToMainActivity() }
                    startProgress.visibility = View.GONE
                    startButton.visibility = View.VISIBLE
                }
            } })
        {
            runOnUiThread {
                Toast.makeText(this@SplashActivity, resources.getText(R.string.cant_update), Toast.LENGTH_SHORT).show()
                binding.apply{
                    startButton.setOnClickListener { navigateToMainActivity() }
                    startProgress.visibility = View.GONE
                    startButton.visibility = View.VISIBLE
                }
            }
        }

        setTransparentStatusBar()
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
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setTransparentStatusBar() {
        val window = window
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }
}