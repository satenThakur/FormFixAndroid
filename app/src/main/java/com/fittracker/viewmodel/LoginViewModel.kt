package com.fittracker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fittracker.APiService.ApiServices
import com.fittracker.model.OTPResponse
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
@HiltViewModel
class LoginViewModel @Inject constructor(val apiServices: ApiServices) :
    ViewModel() {
    private var requestURL = "";
    fun generateOtp(phone: String): LiveData<OTPResponse> {

        var getOTPResponse = MutableLiveData<OTPResponse>()

        apiServices.requestOtp(phone).enqueue(object : Callback<OTPResponse> {
            override fun onResponse(call: Call<OTPResponse>, response: Response<OTPResponse>) {
                // When data is available, populate LiveData
                getOTPResponse.value = response.body()
            }

            override fun onFailure(call: Call<OTPResponse>, t: Throwable) {
                t.printStackTrace()
                var response = OTPResponse()
                response.jsondata = null
                getOTPResponse.value = response
            }
        })
        return getOTPResponse
    }


    fun validateOtp(json: JsonObject): LiveData<OTPResponse> {
        var getOTPResponse = MutableLiveData<OTPResponse>()
        apiServices.validateOtp(json).enqueue(object : Callback<OTPResponse> {
            override fun onResponse(call: Call<OTPResponse>, response: Response<OTPResponse>) {
                // When data is available, populate LiveData
                getOTPResponse.value = response.body()
            }

            override fun onFailure(call: Call<OTPResponse>, t: Throwable) {
                t.printStackTrace()
                var response = OTPResponse()
                response.jsondata = null
                getOTPResponse.value = response
            }
        })
        return getOTPResponse
    }

    fun signup(json: JsonObject): LiveData<OTPResponse> {
        var getOTPResponse = MutableLiveData<OTPResponse>()
        apiServices.signup(json).enqueue(object : Callback<OTPResponse> {
            override fun onResponse(call: Call<OTPResponse>, response: Response<OTPResponse>) {
                // When data is available, populate LiveData
                getOTPResponse.value = response.body()
            }

            override fun onFailure(call: Call<OTPResponse>, t: Throwable) {
                t.printStackTrace()
                var response = OTPResponse()
                response.jsondata = null
                getOTPResponse.value = response
            }
        })
        return getOTPResponse
    }

    fun getLoginWithUserName(module: String, jsonObj: JsonObject): LiveData<OTPResponse> {
        if (module != "pharmacy") {
            requestURL = "auth/"
        }
        var getOTPResponse = MutableLiveData<OTPResponse>()

        apiServices.loginWithUsername(requestURL + module, jsonObj)
            .enqueue(object : Callback<OTPResponse> {
                override fun onResponse(call: Call<OTPResponse>, response: Response<OTPResponse>) {
                    // When data is available, populate LiveData
                    getOTPResponse.value = response.body()
                }

                override fun onFailure(call: Call<OTPResponse>, t: Throwable) {
                    t.printStackTrace()
                    var response = OTPResponse()
                    response.jsondata = null
                    getOTPResponse.value = response
                }
            })
        return getOTPResponse
    }
}