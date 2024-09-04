package com.fittracker.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fittracker.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var activityRegisterBinding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityRegisterBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(activityRegisterBinding.root)
        activityRegisterBinding.backBtn.setOnClickListener {
            onBackPressed()
        }
        activityRegisterBinding.btnRegister.setOnClickListener{
            val intent = Intent(this, VerifyOtpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    override fun onBackPressed() {
        finish()
    }
}