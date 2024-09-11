package com.fittracker.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fittracker.R
import com.fittracker.databinding.ActivityVerifyOtpBinding
import com.fittracker.utilits.FormFixConstants
import com.fittracker.utilits.Utility
import com.fittracker.viewmodel.LoginViewModel
import com.google.gson.JsonObject


class VerifyOtpActivity : AppCompatActivity() {
    private lateinit var activityVerifyOtpBinding: ActivityVerifyOtpBinding
    private val loginViewModel: LoginViewModel by viewModels()
    var phone="";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityVerifyOtpBinding = ActivityVerifyOtpBinding.inflate(layoutInflater)
        setContentView(activityVerifyOtpBinding.root)
        phone= intent.getStringExtra(FormFixConstants.PHONE).toString()
        activityVerifyOtpBinding.lblOtpSent.text="Otp has been sent to "+phone


        activityVerifyOtpBinding.btnResend.setOnClickListener{
            resentOtp()
        }


        activityVerifyOtpBinding.btnConfirm.setOnClickListener{
            var otp=activityVerifyOtpBinding.otpView.text.toString()
          if(isValid(otp)){
              validateOtp(otp)
          }

        }
        activityVerifyOtpBinding.backBtn.setOnClickListener {
            onBackPressed()
        }
        activityVerifyOtpBinding.lblChangePhone.setOnClickListener{
          finish()
        }

    }

    override fun onBackPressed() {
        finish()
    }
    private fun isValid(otp:String):Boolean{
        if(otp.isEmpty()){
            Utility.showErrorSnackBar(activityVerifyOtpBinding.root, resources.getString(R.string.enter_otp))
            return false
        }else if(otp.length<5){
            Utility.showErrorSnackBar(activityVerifyOtpBinding.root, resources.getString(R.string.enter_valid_otp))
            return false
        }
        return true;
    }

    fun validateOtp(otp:String){
        var json=JsonObject()
        json.addProperty("phone", phone)
        json.addProperty("otp", otp)
        loginViewModel.validateOtp(json)?.observe(this) {
            if (it?.statusCode==200) {
                if(intent.getStringExtra(FormFixConstants.ONBOARDING_TYPE)==FormFixConstants.LOGIN) {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    signup();
                }
            }
        }

    }

    fun signup(){
        var json=JsonObject()
        json.addProperty(FormFixConstants.NAME, intent.getStringExtra(FormFixConstants.NAME))
        json.addProperty(FormFixConstants.PHONE, intent.getStringExtra(FormFixConstants.PHONE))
        json.addProperty(FormFixConstants.EMAIL, intent.getStringExtra(FormFixConstants.EMAIL))
        json.addProperty(FormFixConstants.HEIGHT, intent.getStringExtra(FormFixConstants.HEIGHT))
        json.addProperty(FormFixConstants.WEIGHT, intent.getStringExtra(FormFixConstants.WEIGHT))
        loginViewModel.signup(json)?.observe(this) {
            if (it?.statusCode==200) {
                    val intent = Intent(this, DashboardActivity::class.java)
                     intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
            }
        }

    }

    @SuppressLint("SuspiciousIndentation")
    private fun resentOtp(){
        loginViewModel.generateOtp(phone)?.observe(this) {
            if (it?.statusCode==200) {
                Utility.showMessageSnackBar(
                    activityVerifyOtpBinding.root,
                    "Otp has been re-sent to $phone"
                )
                activityVerifyOtpBinding.lblOtpSent.text = "Otp has been re-sent to $phone"
            }
            }
        }


}