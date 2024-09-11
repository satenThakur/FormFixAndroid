package com.fittracker.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class OTPResponse {
    @SerializedName("status")
    @Expose
    var statusCode: Int? = null

    @SerializedName("data")
    @Expose
     var jsondata: DataInfo?=null

    @SerializedName("error")
    @Expose
    var error: String? = null

    data class DataInfo(var message: String)


}