package com.moyasar.android.sdk.payment

import android.util.Base64
import android.util.Base64.encodeToString
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitFactory(
    private val baseUrl: String,
    private val apiKey: String
) {
    fun build(): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor {
                val request = it.request()
                    .newBuilder()
                    .addHeader("Authorization", "Basic ${encodeToString("$apiKey:".toByteArray(), Base64.NO_WRAP)}")
                    .build()

                it.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
