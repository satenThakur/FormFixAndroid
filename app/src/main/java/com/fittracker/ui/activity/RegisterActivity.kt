package com.fittracker.ui.activity

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
            var phone=activityRegisterBinding.edPhone.text.toString().trim()
            var email=activityRegisterBinding.edEmail.text.toString().trim()
            var weight=activityRegisterBinding.edWeight.text.toString().trim()
            var heifhtFT=activityRegisterBinding.edFeet.text.toString().trim()
            var heightInches=activityRegisterBinding.edInches.text.toString().trim()
            Utility.hideKeyboard(this, activityRegisterBinding.btnRegister)
            if (isValid(name,phone,email,weight,heifhtFT,heightInches)) {
                registerApiCall(activityRegisterBinding.ccp.selectedCountryCode,
                    activityRegisterBinding.edPhone.text.toString())
            }
        }
    }
    private fun getHeight(): Int {
        val feetStr = activityRegisterBinding.edFeet.text.toString().trim()
        val inchStr = activityRegisterBinding.edInches.text.toString().trim()
        val feet = feetStr.toIntOrNull() ?: -1
        val inches = inchStr.toIntOrNull() ?: -1
        val totalInches = feet * 12 + inches
        val heightCm = (totalInches * 2.54).toInt()
        return heightCm

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
        heightFt: String,
        heightInches:String
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
        } else if (heightFt.isEmpty()) {
            Utility.showErrorSnackBar(activityRegisterBinding.root, "Enter feet")
            return false
        }else if (heightInches.isEmpty()) {
            Utility.showErrorSnackBar(activityRegisterBinding.root, "Enter inches")
            return false
        }else if(heightFt.toInt() <2 || heightFt.toInt() > 8 ){
            Utility.showErrorSnackBar(
                activityRegisterBinding.root,
                "Feet should be between 2–8"
            )
            return false
        }else if( heightInches.toInt() < 0 || heightInches.toInt() > 11){
            Utility.showErrorSnackBar(activityRegisterBinding.root, "Inches should be 0–11")
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
                    intent.putExtra(FormFixConstants.HEIGHT,""+getHeight())
                    intent.putExtra(FormFixConstants.WEIGHT,activityRegisterBinding.edWeight.text.toString())
                    startActivity(intent)
                    finish()
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