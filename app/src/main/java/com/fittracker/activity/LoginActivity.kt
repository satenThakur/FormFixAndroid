package com.fittracker.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fittracker.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var activityLoginBinding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(activityLoginBinding.root)
        activityLoginBinding.btnLogin.setOnClickListener {
            val intent = Intent(this, VerifyOtpActivity::class.java)
            startActivity(intent)
            finish()
        }

        activityLoginBinding.btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        finish()
    }
}