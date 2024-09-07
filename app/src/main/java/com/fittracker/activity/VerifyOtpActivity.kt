package com.fittracker.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fittracker.APiService.ApiClient
import com.fittracker.R
import com.fittracker.databinding.ActivityVerifyOtpBinding
import com.fittracker.utilits.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.POST


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
            var otp=activityVerifyOtpBinding.otpView.text.toString()
          if(isValid(otp)){
              verifyOtpApiCall(otp)
          }

        }
    }

    override fun onBackPressed() {
        finish()
    }
    private fun isValid(otp:String):Boolean{
        if(otp.isEmpty()){
            Utility.onSNACK(activityVerifyOtpBinding.root, resources.getString(R.string.enter_otp))
            return false
        }else if(otp.length<5){
            Utility.onSNACK(activityVerifyOtpBinding.root, resources.getString(R.string.enter_valid_otp))
            return false
        }
        return true;
    }

    fun verifyOtpApiCall(otp:String){
        val postId = 1 // Replace with the desired post ID
        val call = ApiClient.ApiClient.apiService.getPostById(postId)
        call.enqueue(object : Callback<POST> {
            override fun onResponse(call: Call<POST>, response: Response<POST>) {
                if (response.isSuccessful) {
                    val post = response.body()
                    // Handle the retrieved post data
                    val intent = Intent(this@VerifyOtpActivity, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Handle error
                }
            }

            override fun onFailure(call: Call<POST>, t: Throwable) {
                // Handle failure
            }
        })

    }
    fun resentOtpApiCall(phone:String){
        val postId = 1 // Replace with the desired post ID
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
        })

    }
}