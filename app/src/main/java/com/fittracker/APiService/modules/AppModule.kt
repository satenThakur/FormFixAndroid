package com.lookuptalk.professional.di.modules

import android.content.Context
import com.fittracker.APiService.ApiConstants
import com.fittracker.APiService.ApiServices
import com.fittracker.application.FormfitApplication
import com.fittracker.model.OTPResponse
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplication(@ApplicationContext context: Context): FormfitApplication {
        return context as FormfitApplication
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    @Provides
    @Singleton
    fun provideClient(interceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                // Add Auth Header
               // val token: String = prefs.getAccessToken()
                val request: Request = chain.request().newBuilder().addHeader("Authorization", "Bearer ").build()
                var response = chain.proceed(request)
                if (response.code == 401) {
                    // Refresh the token
                   /* Log.d(
                        "AppModule",
                        "Trying to fetch new token using refresh token: " + prefs.getRefreshAccessToken()
                    )*/
                    val client = OkHttpClient()
                    val params = JSONObject()
                    val body: RequestBody = RequestBody.create(response.body?.contentType(), "")
                    // retry the request
                   // val refreshToken: String = prefs.getRefreshAccessToken()
                   /* val nRequest = Request.Builder()
                        .post(body)
                        .header("Authorization", "Bearer " + refreshToken)
                        .header("accept", "application/json")
                        .url("https://services.eazyop.in/api/v1/refreshToken")
                        .build()*/
                    val nRequest = Request.Builder()
                        .post(body)
                        .header("accept", "application/json")
                      /*  .url("https://services.eazyop.in/api/v1/refreshToken")*/
                        .build()

                    var newResponse = client.newCall(nRequest).execute()
                    if (newResponse.code.toString().startsWith("2")) {
                        val success = Gson().fromJson(newResponse.body?.string(), OTPResponse::class.java)
                        // save token and refresh token
                        /*prefs.setAccessToken(
                            success.jsondata?.token!!,
                            success.jsondata?.refreshToken!!
                        )*/
                        // Get response
                        val request: Request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " /*+ prefs.getAccessToken()*/).build()
                        response = chain.proceed(request)
                    }
                }
                response
            })
            .addNetworkInterceptor(interceptor).connectTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES).writeTimeout(5, TimeUnit.MINUTES)
            .connectionPool(ConnectionPool(0, 1, TimeUnit.SECONDS)).retryOnConnectionFailure(true)
            .build()
    }

    @Singleton
    @Provides
    fun providesRetrofitService(client: OkHttpClient): ApiServices {
        return Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(ApiServices::class.java)
    }


}