package com.fittracker.ui.activity

import android.R
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.fittracker.databinding.ActivitySplashBinding
import com.fittracker.utilits.ConstantsSquats.SPLASH_DELAY
import com.fittracker.utilits.FormFixConstants
import com.fittracker.utilits.FormFixSharedPreferences
import dagger.hilt.android.AndroidEntryPoint


class SplashActivity : AppCompatActivity() {
    private lateinit var activitySplashBinding: ActivitySplashBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySplashBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(activitySplashBinding.root)
        val window: Window = this.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        Handler(Looper.getMainLooper()).postDelayed({
            moveToNextScreen()
        }, SPLASH_DELAY)

    }

    fun moveToNextScreen(){
        if(FormFixSharedPreferences.getSharedPrefBooleanValue(this@SplashActivity, FormFixConstants.IS_USER_LOGEDIN)){
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }else{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        finish()
    }
}