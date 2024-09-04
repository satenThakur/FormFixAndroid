package com.fittracker.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fittracker.databinding.ActivityVerifyOtpBinding


class VerifyOtpActivity : AppCompatActivity() {
    private lateinit var activityVerifyOtpBinding: ActivityVerifyOtpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityVerifyOtpBinding = ActivityVerifyOtpBinding.inflate(layoutInflater)
        setContentView(activityVerifyOtpBinding.root)
        activityVerifyOtpBinding.backBtn.setOnClickListener {
            onBackPressed()
        }
        activityVerifyOtpBinding.lblChangePhone.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        activityVerifyOtpBinding.btnConfirm.setOnClickListener{
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        finish()
    }
}