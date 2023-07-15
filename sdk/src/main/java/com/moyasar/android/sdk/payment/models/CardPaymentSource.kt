package com.moyasar.android.sdk.payment.models

import com.google.gson.annotations.SerializedName

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
