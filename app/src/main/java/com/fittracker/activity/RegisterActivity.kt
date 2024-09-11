package com.fittracker.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fittracker.R
import com.fittracker.databinding.ActivityRegisterBinding
import com.fittracker.utilits.FormFixConstants
import com.fittracker.utilits.Utility
import com.fittracker.viewmodel.LoginViewModel
import okhttp3.internal.notifyAll
import okhttp3.internal.wait

class RegisterActivity : AppCompatActivity() {
    private lateinit var activityRegisterBinding: ActivityRegisterBinding
    private val loginViewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityRegisterBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(activityRegisterBinding.root)
        activityRegisterBinding.backBtn.setOnClickListener {
            onBackPressed()
        }
        activityRegisterBinding.btnRegister.setOnClickListener {
            val intent = Intent(this, VerifyOtpActivity::class.java)
            startActivity(intent)
            finish()
        }
        activityRegisterBinding.btnRegister.setOnClickListener {
            var name=activityRegisterBinding.edName.text.toString()
            var phone=activityRegisterBinding.edPhone.text.toString()
            var email=activityRegisterBinding.edEmail.text.toString()
            var weight=activityRegisterBinding.edWeight.text.toString()
            var height=activityRegisterBinding.edHeight.text.toString()
            if (isValid(name,phone,email,weight,height)) {
                registerApiCall(name,phone,email,weight,height)
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
    private fun generateOtp(countryCode:String, phone:String){
        var phoneNumber= "+$countryCode$phone"
        loginViewModel.generateOtp(phoneNumber)?.observe(this) {
            if (it?.statusCode==200) {
                val intent = Intent(this, VerifyOtpActivity::class.java)
                intent.putExtra(FormFixConstants.ONBOARDING_TYPE,FormFixConstants.REGISTER)
                intent.putExtra(FormFixConstants.NAME,activityRegisterBinding.edName.text)
                intent.putExtra(FormFixConstants.PHONE,phoneNumber)
                intent.putExtra(FormFixConstants.EMAIL,activityRegisterBinding.edEmail.text)
                intent.putExtra(FormFixConstants.HEIGHT,activityRegisterBinding.edHeight.text)
                intent.putExtra(FormFixConstants.WEIGHT,activityRegisterBinding.edWeight.text)
                startActivity(intent)
            }
        }

    }
    private fun registerApiCall(
        name: String,
        phone: String,
        email: String,
        weight: String,
        height: String
    ) {

/*        val postId = 1 // Replace with the desired post ID
        val call = ApiClient.ApiClient.apiService.getPostById(postId)
        call.enqueue(object : Callback<POST> {
            override fun onResponse(call: Call<POST>, response: Response<POST>) {
                if (response.isSuccessful) {
                    val post = response.body()
                    // Handle the retrieved post data
                } else {
                    // Handle error
                }
            }

            override fun onFailure(call: Call<POST>, t: Throwable) {
                // Handle failure
            }
        })*/

    }
}