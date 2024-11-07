package com.fittracker.APiService

import com.fittracker.model.ApiResponse
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiServices {
    /**On-Boarding Flow Apis **/

    @GET("/api/v1/user/login/{phone}")
    fun userLogin(@Path(value = "phone", encoded = true) phone: String): Call<ApiResponse>

    @POST("/api/v1/generateOtp/{phone}")
    fun requestOtp(@Path(value = "phone", encoded = true) phone: String): Call<ApiResponse>


    @POST("/api/v1/validateOtp")
    fun validateOtp(@Body jsonObject: JsonObject
    ): Call<ApiResponse>
    @POST("/api/v1/user/signup")
    fun signup(@Body jsonObject: JsonObject
    ): Call<ApiResponse>

    @POST("/api/v1/{request_for}/register")
    fun register(
        @Path(value = "request_for", encoded = true) request_for: String,
        @Body jsonObject: JsonObject
    ): Call<ResponseBody>

    /*Patient Request OTP*/

    /*Login patient*/
    @POST("/api/v1/{request_for}/login")
    fun loginWithUsername(
        @Path(value = "request_for", encoded = true) request_for: String,
        @Body jsonObject: JsonObject
    ): Call<ApiResponse>

}
