package com.moyasar.android.sdk.payment

import com.moyasar.android.sdk.payment.models.Payment
import com.moyasar.android.sdk.payment.models.PaymentRequest
import khttp.post

class PaymentService(private val baseUrl: String) {
    suspend fun create(request: PaymentRequest): Payment {
        val createUrl = getResourceUrl("v1/payments")
        val response = post(createUrl, json = request)

    }

    private fun getResourceUrl(url: String): String {
        return baseUrl.trimEnd('/').trimEnd() +
            url.trimStart('/').trimStart()
    }
}
