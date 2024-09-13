package com.fittracker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fittracker.APiService.ApiServices
import com.fittracker.model.ApiResponse
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
@HiltViewModel
class OnBoardingViewModel @Inject constructor(val apiServices: ApiServices) :
    ViewModel() {
    private var requestURL = "";
    fun userLogin(phone: String): MutableLiveData<ApiResponse?> {
        var loginResponse = MutableLiveData<ApiResponse?>()
        apiServices.userLogin(phone).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                loginResponse.value = response.body()
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                loginResponse.value = null
            }
        })
        return loginResponse
    }

    fun generateOtp(phone: String): LiveData<ApiResponse> {
        var genrateOtpResponse = MutableLiveData<ApiResponse>()
        apiServices.requestOtp(phone).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                genrateOtpResponse.value = response.body()
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                t.printStackTrace()
                genrateOtpResponse.value = null
            }
        })
        return genrateOtpResponse
    }


    fun validateOtp(json: JsonObject): LiveData<ApiResponse> {
        var validateOtpresponse = MutableLiveData<ApiResponse>()
        apiServices.validateOtp(json).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                // When data is available, populate LiveData
                validateOtpresponse.value = response.body()
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                t.printStackTrace()
                validateOtpresponse.value = null
            }
        })
        return validateOtpresponse
    }

    fun signup(json: JsonObject): LiveData<ApiResponse> {
        var signupResponse = MutableLiveData<ApiResponse>()
        apiServices.signup(json).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                signupResponse.value = response.body()
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                t.printStackTrace()
                signupResponse.value = null
            }
        })
        return signupResponse
    }

}