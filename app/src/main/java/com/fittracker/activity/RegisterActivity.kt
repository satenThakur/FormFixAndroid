package com.fittracker.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fittracker.R
import com.fittracker.databinding.ActivityRegisterBinding
import com.fittracker.utilits.FormFixConstants
import com.fittracker.utilits.Utility
import com.fittracker.viewmodel.OnBoardingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    private lateinit var activityRegisterBinding: ActivityRegisterBinding
    private val loginViewModel: OnBoardingViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityRegisterBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(activityRegisterBinding.root)
        activityRegisterBinding.backBtn.setOnClickListener {
            onBackPressed()
        }

        activityRegisterBinding.btnRegister.setOnClickListener {
            var name=activityRegisterBinding.edName.text.toString()
            var phone=activityRegisterBinding.edPhone.text.toString()
            var email=activityRegisterBinding.edEmail.text.toString()
            var weight=activityRegisterBinding.edWeight.text.toString()
            var height=activityRegisterBinding.edHeight.text.toString()
            Utility.hideKeyboard(this, activityRegisterBinding.btnRegister)
            if (isValid(name,phone,email,weight,height)) {
                registerApiCall(activityRegisterBinding.ccp.selectedCountryCode,
                    activityRegisterBinding.edPhone.text.toString())
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()

    }

    private fun isValid(
        name: String,
        phone: String,
        email: String,
        weight: String,
        height: String
    ): Boolean {
        if (name.isEmpty()) {
            Utility.showErrorSnackBar(
                activityRegisterBinding.root,
                resources.getString(R.string.enter_name)
            )
            return false
        } else if (phone.isEmpty()) {
            Utility.showErrorSnackBar(
                activityRegisterBinding.root,
                resources.getString(R.string.enter_phone_number)
            )
            return false
        } else if (phone.length < 10) {
            Utility.showErrorSnackBar(
                activityRegisterBinding.root,
                resources.getString(R.string.enter_correct_phone_number)
            )
            return false
        } else if (email.isEmpty()) {
            Utility.showErrorSnackBar(
                activityRegisterBinding.root,
                resources.getString(R.string.enter_email)
            )
            return false
        } else if (!Utility.isValidEmail(email.toString())) {
            Utility.showErrorSnackBar(
                activityRegisterBinding.root,
                resources.getString(R.string.enter_valid_email)
            )
            return false
        } else if (weight.isEmpty()) {
            Utility.showErrorSnackBar(
                activityRegisterBinding.root,
                resources.getString(R.string.enter_weight)
            )
            return false
        } else if (height.isEmpty()) {
            Utility.showErrorSnackBar(
                activityRegisterBinding.root,
                resources.getString(R.string.enter_height)
            )
            return false
        }
        return true

    }
    @SuppressLint("SuspiciousIndentation")
    private fun registerApiCall(countryCode:String, phone:String){
        activityRegisterBinding.progressCircular.visibility = View.VISIBLE
        activityRegisterBinding.progressCircular.bringToFront()
        var phoneNumber= "+$countryCode$phone"
        loginViewModel.generateOtp(phoneNumber)?.observe(this) {
            activityRegisterBinding.progressCircular.visibility = View.GONE
            if (it?.status == 200) {
                if(it?.data?.responseData?.code== FormFixConstants.SUCCESS) {
                    val intent = Intent(this, VerifyOtpActivity::class.java)
                    intent.putExtra(FormFixConstants.ONBOARDING_TYPE,FormFixConstants.REGISTER)
                    intent.putExtra(FormFixConstants.NAME,activityRegisterBinding.edName.text.toString())
                    intent.putExtra(FormFixConstants.PHONE,phoneNumber)
                    intent.putExtra(FormFixConstants.EMAIL,activityRegisterBinding.edEmail.text.toString())
                    intent.putExtra(FormFixConstants.HEIGHT,activityRegisterBinding.edHeight.text.toString())
                    intent.putExtra(FormFixConstants.WEIGHT,activityRegisterBinding.edWeight.text.toString())
                    startActivity(intent)
                }else if(it?.data?.responseData?.code== FormFixConstants.FAILED){
                    activityRegisterBinding.progressCircular.visibility = View.GONE
                    it?.data?.responseData?.message?.let { it1 ->
                        Utility.showDialog(this@RegisterActivity,"Error",
                            it1
                        )
                    }
                }
            } else {
                activityRegisterBinding.progressCircular.visibility = View.GONE
                Utility.showDialog(this@RegisterActivity,"Error",
                    resources.getString(R.string.something_went_wrong)
                )
            }


        }

    }

}