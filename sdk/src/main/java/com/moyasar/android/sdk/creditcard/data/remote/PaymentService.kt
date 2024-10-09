package com.moyasar.android.sdk.creditcard.data.remote

import com.google.gson.Gson
import com.moyasar.android.sdk.core.exceptions.ApiException
import com.moyasar.android.sdk.core.extensions.getResourceUrlFormated
import com.moyasar.android.sdk.core.extensions.postJson
import com.moyasar.android.sdk.core.extensions.setBasicAuth
import com.moyasar.android.sdk.core.data.response.ErrorResponse
import com.moyasar.android.sdk.core.data.response.PaymentResponse
import com.moyasar.android.sdk.core.util.MoyasarLogger
import com.moyasar.android.sdk.creditcard.data.models.request.PaymentRequest
import com.moyasar.android.sdk.creditcard.data.models.response.TokenResponse
import com.moyasar.android.sdk.creditcard.data.models.request.TokenRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class PaymentService(
    private val apiKey: String,
    private val baseUrl: String,
    private val gson: Gson = Gson()
) {

    companion object {
        private const val TAG = "PaymentService"
    }

    suspend fun create(request: PaymentRequest): PaymentResponse = withContext(Dispatchers.IO) {
        val createUrl = baseUrl.getResourceUrlFormated("v1/payments")
        val client = URL(createUrl).openConnection() as HttpURLConnection

        client.setBasicAuth(apiKey, "")
        // Log the request
        MoyasarLogger.log(TAG, "Request URL: $createUrl")
        MoyasarLogger.log(TAG, "Request Body: ${gson.toJson(request)}")
        val response = client.postJson(request)
        // Log the response
        MoyasarLogger.log(TAG, "Response Code: ${response.statusCode}")
        MoyasarLogger.log(TAG, "Response Body: ${response.text}")

        if (response.statusCode !in 200..299) {
            throw ApiException(
                gson.fromJson(response.text, ErrorResponse::class.java)
            )
        }

        gson.fromJson(response.text, PaymentResponse::class.java)
    }

    suspend fun createToken(request: TokenRequest): TokenResponse = withContext(Dispatchers.IO) {
        val createUrl = baseUrl.getResourceUrlFormated("v1/tokens")
        val client = URL(createUrl).openConnection() as HttpURLConnection

        client.setBasicAuth(apiKey, "")
        val response = client.postJson(request)

        if (response.statusCode !in 200..299) {
            throw ApiException(
                gson.fromJson(response.text, ErrorResponse::class.java)
            )
        }

        gson.fromJson(response.text, TokenResponse::class.java)
    }
}
