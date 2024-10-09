package com.moyasar.android.sdk.creditcard.data.models.sources

import com.google.gson.annotations.SerializedName
import com.moyasar.android.sdk.core.data.PaymentSource

data class CardPaymentSource(
    val name: String,
    val number: String,
    val month: String,
    val year: String,
    val cvc: String,
    val manual: String?,
    @SerializedName("save_card") val saveCard: String?,
    val type: String = "creditcard",
    val token: String? = null,
) : PaymentSource
