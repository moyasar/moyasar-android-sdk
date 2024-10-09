package com.moyasar.android.sdk.stcpay.data.remote

import com.google.gson.Gson
import com.moyasar.android.sdk.core.exceptions.ApiException
import com.moyasar.android.sdk.core.extensions.postJson
import com.moyasar.android.sdk.core.data.response.ErrorResponse
import com.moyasar.android.sdk.core.data.response.PaymentResponse
import com.moyasar.android.sdk.core.util.MoyasarLogger
import com.moyasar.android.sdk.stcpay.data.models.request.STCPayOTPRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by Mahmoud Ashraf on 22,September,2024
 */
class STCPayPaymentService(
    private val gson: Gson = Gson()
) {
    companion object {
        private const val TAG = "STCPayPaymentService"
    }

    suspend fun validateSTCPayOTP(transactionURL: String, otp: String): PaymentResponse = withContext(Dispatchers.IO) {
        val client = URL(transactionURL).openConnection() as HttpURLConnection
        val request = STCPayOTPRequest(otp)

        // Log the request
        MoyasarLogger.log(TAG, "Request URL: $transactionURL")
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

}