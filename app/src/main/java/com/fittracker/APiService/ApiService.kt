package com.fittracker.APiService

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("posts/{id}")
    fun getLogin(@Path("id") postId: Int): Call<POST>
}