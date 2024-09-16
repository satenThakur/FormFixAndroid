package com.fittracker.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fittracker.R
import com.fittracker.databinding.ActivityVerifyOtpBinding
import com.fittracker.utilits.FormFixConstants
import com.fittracker.utilits.FormFixSharedPreferences
import com.fittracker.utilits.Utility
import com.fittracker.utilits.Utility.saveUser
import com.fittracker.viewmodel.OnBoardingViewModel
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerifyOtpActivity : AppCompatActivity() {
    private lateinit var activityVerifyOtpBinding: ActivityVerifyOtpBinding
    private val loginViewModel: OnBoardingViewModel by viewModels()
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
            Utility.hideKeyboard(this, activityVerifyOtpBinding.btnConfirm)
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

    private fun validateOtp(otp:String){
        activityVerifyOtpBinding.progressCircular.bringToFront()
        activityVerifyOtpBinding.progressCircular.visibility=View.VISIBLE
        var json=JsonObject()
        json.addProperty("phone", phone)
        json.addProperty("otp", otp)
        loginViewModel.validateOtp(json)?.observe(this) {
            activityVerifyOtpBinding.progressCircular.visibility=View.GONE
            if (it?.status == 200) {
                if(it?.data?.responseData?.code== FormFixConstants.SUCCESS) {
                    if(intent.getStringExtra(FormFixConstants.ONBOARDING_TYPE)==FormFixConstants.LOGIN) {
                        Utility.saveUser(it?.data?.responseData?.user,this@VerifyOtpActivity)
                        val intent = Intent(this, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        registerUser();
                    }
                }else if(it?.data?.responseData?.code== FormFixConstants.FAILED){
                    it?.data?.responseData?.message?.let { it1 ->
                        Utility.showDialog(this@VerifyOtpActivity,"Error",
                            it1
                        )
                    }
                }
            } else {
                Utility.showDialog(this@VerifyOtpActivity,"Error",
                    resources.getString(R.string.something_went_wrong)
                )
            }

        }

    }

    private fun registerUser(){
        var json=JsonObject()
        json.addProperty(FormFixConstants.NAME, intent.getStringExtra(FormFixConstants.NAME))
        json.addProperty(FormFixConstants.PHONE, intent.getStringExtra(FormFixConstants.PHONE))
        json.addProperty(FormFixConstants.EMAIL, intent.getStringExtra(FormFixConstants.EMAIL))
        json.addProperty(FormFixConstants.HEIGHT, intent.getStringExtra(FormFixConstants.HEIGHT))
        json.addProperty(FormFixConstants.WEIGHT, intent.getStringExtra(FormFixConstants.WEIGHT))
        loginViewModel.signup(json)?.observe(this) {
            if (it?.status == 200) {
                if(it?.data?.responseData?.code== FormFixConstants.SUCCESS) {
                    Utility.saveUser(it?.data?.responseData?.user,this@VerifyOtpActivity)
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }else if(it?.data?.responseData?.code== FormFixConstants.FAILED){
                    it?.data?.responseData?.message?.let { it1 ->
                        Utility.showDialog(this@VerifyOtpActivity,"Error",
                            it1
                        )
                    }
                }
            } else {
                Utility.showDialog(this@VerifyOtpActivity,"Error",
                    resources.getString(R.string.something_went_wrong)
                )
            }
        }

    }

    @SuppressLint("SuspiciousIndentation")
    private fun resentOtp(){
        activityVerifyOtpBinding.progressCircular.bringToFront()
        activityVerifyOtpBinding.progressCircular.visibility= View.VISIBLE
        loginViewModel.generateOtp(phone)?.observe(this) {
            activityVerifyOtpBinding.progressCircular.visibility= View.GONE
            if (it?.status == 200) {
                if(it?.data?.responseData?.code== FormFixConstants.SUCCESS) {
                    Utility.showMessageSnackBar(
                        activityVerifyOtpBinding.root,
                        "Otp has been re-sent to $phone"
                    )
                    activityVerifyOtpBinding.lblOtpSent.text = "Otp has been re-sent to $phone"
                }else if(it?.data?.responseData?.code== FormFixConstants.FAILED){
                    it?.data?.responseData?.message?.let { it1 ->
                        Utility.showDialog(this@VerifyOtpActivity,"Error",
                            it1
                        )
                    }
                }
            } else {
                Utility.showDialog(this@VerifyOtpActivity,"Error",
                    resources.getString(R.string.something_went_wrong)
                )
            }

            }
        }


}