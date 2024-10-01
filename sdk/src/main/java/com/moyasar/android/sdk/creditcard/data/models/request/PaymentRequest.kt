package com.moyasar.android.sdk.creditcard.data.models.request

import com.google.gson.annotations.SerializedName
import com.moyasar.android.sdk.core.data.PaymentSource

data class PaymentRequest(
    val amount: Int,
    val currency: String,
    val description: String?,
    @SerializedName("callback_url") val callbackUrl: String,
    val source: PaymentSource,
    val metadata: Map<String, Any?> = HashMap()
)
