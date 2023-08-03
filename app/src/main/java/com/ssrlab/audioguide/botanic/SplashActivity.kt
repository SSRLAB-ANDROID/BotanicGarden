package com.ssrlab.audioguide.botanic

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.ssrlab.audioguide.botanic.client.ExhibitClient
import com.ssrlab.audioguide.botanic.databinding.ActivitySplashBinding
import com.ssrlab.audioguide.botanic.db.ExhibitDao
import com.ssrlab.audioguide.botanic.db.ExhibitDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var exhibitDao: ExhibitDao

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = Room.databaseBuilder(applicationContext, ExhibitDatabase::class.java, "exhibit_table")
            .fallbackToDestructiveMigration()
            .build()
        exhibitDao = db.exhibitDao()

        ExhibitClient.getExhibits(scope, exhibitDao) {
            runOnUiThread { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }

        binding.startButton.setOnClickListener {
            navigateToMainActivity()
        }

        setTransparentStatusBar()
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