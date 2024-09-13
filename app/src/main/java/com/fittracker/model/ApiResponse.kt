package com.fittracker.model


import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ApiResponse(@SerializedName("status") var status : Int? = null,
    @SerializedName("data") var data    : Data,
    @SerializedName("error") var error : String? = null)

    data class Data (
        @SerializedName("responseData") var responseData  : ResponseData?= null
    )

data class ResponseData (
    @SerializedName("code") var code          : Int?      = null,
    @SerializedName("message") var message   : String?      = null,
    @SerializedName("data") var user    : User?      = null
)
data class User (
    @SerializedName("id"         ) var id          : Int?      = null,
    @SerializedName("name"   ) var name   : String?      = null,
    @SerializedName("phone"    ) var phone    :  String?      = null,
    @SerializedName("email"    ) var email    :  String?      = null,
    @SerializedName("weight"    ) var weight    :  String?      = null,
    @SerializedName("height"    ) var height    :  String?      = null
): Serializable

