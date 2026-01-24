package com.app.lovelyprints.core.network

import android.util.Log
import com.app.lovelyprints.core.auth.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(
    private val tokenManager: TokenManager,
    private val onUnauthorized: () -> Unit
) {

    companion object {
        private const val BASE_URL =
            "PUT_API_HERE"
    }

    private val authInterceptor = Interceptor { chain ->

        val token = tokenManager.getTokenBlocking()

        Log.d("AUTH", "JWT = $token")

        val newRequest = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()

        Log.d("AUTH", "HEADERS = ${newRequest.headers}")

        val response = chain.proceed(newRequest)

        Log.d("AUTH", "RESPONSE CODE = ${response.code}")

        if (response.code == 401) {
            onUnauthorized()
        }

        response
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    inline fun <reified T> createService(): T =
        retrofit.create(T::class.java)
}
