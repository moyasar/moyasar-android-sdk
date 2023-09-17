package com.moyasar.android.sdk.payment

import com.google.gson.Gson
import com.moyasar.android.sdk.exceptions.ApiException
import com.moyasar.android.sdk.extensions.postJson
import com.moyasar.android.sdk.extensions.setBasicAuth
import com.moyasar.android.sdk.payment.models.ErrorResponse
import com.moyasar.android.sdk.payment.models.Payment
import com.moyasar.android.sdk.payment.models.PaymentRequest
import com.moyasar.android.sdk.payment.models.Token
import com.moyasar.android.sdk.payment.models.TokenRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class PaymentService(
    private val apiKey: String,
    private val baseUrl: String
) {
    private val gson = Gson()

    suspend fun create(request: PaymentRequest): Payment = withContext(Dispatchers.IO) {
        val createUrl = getResourceUrl("v1/payments")
        val client = URL(createUrl).openConnection() as HttpURLConnection

        client.setBasicAuth(apiKey, "")
        val response = client.postJson(request)

        if (response.statusCode !in 200..299) {
            throw ApiException(
                gson.fromJson(response.text, ErrorResponse::class.java)
            )
        }

        gson.fromJson(response.text, Payment::class.java)
    }

    suspend fun createToken(request: TokenRequest): Token = withContext(Dispatchers.IO) {
        val createUrl = getResourceUrl("v1/tokens")
        val client = URL(createUrl).openConnection() as HttpURLConnection

        client.setBasicAuth(apiKey, "")
        val response = client.postJson(request)

        if (response.statusCode !in 200..299) {
            throw ApiException(
                gson.fromJson(response.text, ErrorResponse::class.java)
            )
        }

        gson.fromJson(response.text, Token::class.java)
    }

    private fun getResourceUrl(url: String): String {
        return baseUrl.trimEnd('/').trimEnd() + "/" +
            url.trimStart('/').trimStart()
    }
}
