package com.fittracker.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fittracker.R
import com.fittracker.databinding.ActivityLoginBinding
import com.fittracker.utilits.FormFixConstants
import com.fittracker.utilits.Utility
import com.fittracker.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var activityLoginBinding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(activityLoginBinding.root)

        activityLoginBinding.btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
        activityLoginBinding.btnLogin.setOnClickListener{
            if(isValid()) {
                generateOtp(activityLoginBinding.ccp.selectedCountryCode,activityLoginBinding.etPhoneNumber.text.toString())
            }
        }

    }

    override fun onBackPressed() {
        finish()
    }

    private fun isValid():Boolean{
        if(activityLoginBinding.etPhoneNumber.text.toString().isBlank()){
            Utility.showErrorSnackBar(activityLoginBinding.root,
                resources.getString(R.string.enter_phone_number)
            )
            return false
        } else if(activityLoginBinding.etPhoneNumber.text.toString().length<10){
            Utility.showErrorSnackBar(activityLoginBinding.root, resources.getString(R.string.enter_correct_phone_number))
            return false
        }
        return true
    }
    @SuppressLint("SuspiciousIndentation")
    private fun generateOtp(countryCode:String, phone:String){
      var phoneNumber="+"+countryCode+phone
        loginViewModel.generateOtp(phoneNumber)?.observe(this) {
            if (it?.statusCode==200) {
                val intent = Intent(this, VerifyOtpActivity::class.java)
                intent.putExtra(FormFixConstants.ONBOARDING_TYPE,FormFixConstants.REGISTER)
                intent.putExtra(FormFixConstants.PHONE,phoneNumber)
                startActivity(intent)
                finish()
            }
        }

    }
}