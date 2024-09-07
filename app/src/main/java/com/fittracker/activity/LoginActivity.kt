package com.fittracker.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fittracker.APiService.ApiClient
import com.fittracker.R
import com.fittracker.databinding.ActivityLoginBinding
import com.fittracker.utilits.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.POST


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
        activityLoginBinding.btnLogin.setOnClickListener{
            if(isValid()) {
                loginApi(activityLoginBinding.ccp.selectedCountryCode,activityLoginBinding.etPhoneNumber.text.toString())
            }
        }

        activityLoginBinding.ccp.setOnClickListener{ // getting the country code
            val countryCode: String = activityLoginBinding.ccp.selectedCountryCode
            val countryName: String =  activityLoginBinding.ccp.selectedCountryName
            val countryNameCode: String =  activityLoginBinding.ccp.selectedCountryNameCode
            Toast.makeText(
                this@LoginActivity,
                "Country Name:-$countryName Country Name Code:-$countryNameCode Country Code:-$countryCode",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    override fun onBackPressed() {
        finish()
    }

    private fun isValid():Boolean{
        if(activityLoginBinding.etPhoneNumber.text.toString().isBlank()){
            Utility.onSNACK(activityLoginBinding.root,
                resources.getString(R.string.enter_phone_number)
            )
            return false
        } else if(activityLoginBinding.etPhoneNumber.text.toString().length<10){
            Utility.onSNACK(activityLoginBinding.root, resources.getString(R.string.enter_correct_phone_number))
            return false
        }
        return true
    }
    private fun loginApi(countryCode:String,phoneNumber:String){
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