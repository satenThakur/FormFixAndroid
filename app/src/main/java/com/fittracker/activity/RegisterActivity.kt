package com.fittracker.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fittracker.APiService.ApiClient
import com.fittracker.R
import com.fittracker.databinding.ActivityRegisterBinding
import com.fittracker.utilits.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.POST

class RegisterActivity : AppCompatActivity() {
    private lateinit var activityRegisterBinding: ActivityRegisterBinding
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
            var phone=activityRegisterBinding.edName.text.toString()
            var email=activityRegisterBinding.edName.text.toString()
            var weight=activityRegisterBinding.edName.text.toString()
            var height=activityRegisterBinding.edName.text.toString()
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
            Utility.onSNACK(
                activityRegisterBinding.root,
                resources.getString(R.string.enter_name)
            )
            return false
        } else if (phone.isEmpty()) {
            Utility.onSNACK(
                activityRegisterBinding.root,
                resources.getString(R.string.enter_phone_number)
            )
            return false
        } else if (phone.length < 10) {
            Utility.onSNACK(
                activityRegisterBinding.root,
                resources.getString(R.string.enter_correct_phone_number)
            )
            return false
        } else if (email.isEmpty()) {
            Utility.onSNACK(
                activityRegisterBinding.root,
                resources.getString(R.string.enter_email)
            )
            return false
        } else if (!Utility.isValidEmail(email.toString())) {
            Utility.onSNACK(
                activityRegisterBinding.root,
                resources.getString(R.string.enter_valid_email)
            )
            return false
        } else if (weight.isEmpty()) {
            Utility.onSNACK(
                activityRegisterBinding.root,
                resources.getString(R.string.enter_weight)
            )
            return false
        } else if (height.isEmpty()) {
            Utility.onSNACK(
                activityRegisterBinding.root,
                resources.getString(R.string.enter_height)
            )
            return false
        }
        return true

    }

    private fun registerApiCall(
        name: String,
        phone: String,
        email: String,
        weight: String,
        height: String
    ) {
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