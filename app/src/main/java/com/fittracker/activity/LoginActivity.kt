package com.fittracker.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fittracker.R
import com.fittracker.databinding.ActivityLoginBinding
import com.fittracker.utilits.FormFixConstants
import com.fittracker.utilits.FormFixConstants.FAILED
import com.fittracker.utilits.FormFixConstants.SUCCESS
import com.fittracker.utilits.Utility
import com.fittracker.utilits.Utility.hideKeyboard
import com.fittracker.viewmodel.OnBoardingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var activityLoginBinding: ActivityLoginBinding
    private val onBoardingViewModel: OnBoardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(activityLoginBinding.root)
        activityLoginBinding.btnLogin.setOnClickListener {
            hideKeyboard(this,activityLoginBinding.btnLogin)
            if (isValid()) {
                userLogin(activityLoginBinding.ccp.selectedCountryCode, activityLoginBinding.etPhoneNumber.text.toString()
                )
            }
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

    private fun isValid(): Boolean {
        if (activityLoginBinding.etPhoneNumber.text.toString().isBlank()) {
            Utility.showErrorSnackBar(
                activityLoginBinding.root,
                resources.getString(R.string.enter_phone_number)
            )
            return false
        } else if (activityLoginBinding.etPhoneNumber.text.toString().length < 10) {
            Utility.showErrorSnackBar(
                activityLoginBinding.root,
                resources.getString(R.string.enter_correct_phone_number)
            )
            return false
        }
        return true
    }
    @SuppressLint("SuspiciousIndentation")
    private fun userLogin(countryCode: String, phone: String) {
        activityLoginBinding.progressCircular.visibility = View.VISIBLE
        activityLoginBinding.progressCircular.bringToFront()
        var phoneNumber = "+$countryCode$phone"
        onBoardingViewModel.userLogin(phoneNumber)?.observe(this) {
            if (it?.status == 200) {
                if(it?.data?.responseData?.code==SUCCESS) {
                generateOtp(countryCode, phone)
                }else if(it?.data?.responseData?.code==FAILED){
                    activityLoginBinding.progressCircular.visibility = View.GONE
                    it?.data?.responseData?.message?.let { it1 ->
                        Utility.showDialog(this@LoginActivity,"Error",
                            it1
                        )
                    }
                }
            } else {
                activityLoginBinding.progressCircular.visibility = View.GONE
                Utility.showDialog(this@LoginActivity,"Error",
                    resources.getString(R.string.something_went_wrong)
                )
            }
        }

    }
    @SuppressLint("SuspiciousIndentation")
    private fun generateOtp(countryCode: String, phone: String) {
        activityLoginBinding.progressCircular.visibility = View.VISIBLE
        activityLoginBinding.progressCircular.bringToFront()
        var phoneNumber = "+$countryCode$phone"
        onBoardingViewModel.generateOtp(phoneNumber)?.observe(this) {
            activityLoginBinding.progressCircular.visibility = View.GONE
            if (it?.status == 200) {
                if(it?.data?.responseData?.code==SUCCESS) {
                    val intent = Intent(this, VerifyOtpActivity::class.java)
                    intent.putExtra(FormFixConstants.ONBOARDING_TYPE, FormFixConstants.LOGIN)
                    intent.putExtra(FormFixConstants.PHONE, phoneNumber)
                    startActivity(intent)
                    finish()
                }else if(it?.data?.responseData?.code==FAILED){
                    activityLoginBinding.progressCircular.visibility = View.GONE
                    it?.data?.responseData?.message?.let { it1 ->
                        Utility.showDialog(this@LoginActivity,"Error",
                            it1
                        )
                    }
                }
            } else {
                activityLoginBinding.progressCircular.visibility = View.GONE
                Utility.showDialog(this@LoginActivity,"Error",
                    resources.getString(R.string.something_went_wrong)
                )
            }

        }

    }
}