package com.moyasar.android.sdk.creditcard.data.models.sources

import com.google.gson.annotations.SerializedName
import com.moyasar.android.sdk.core.data.PaymentSource

data class CardPaymentSource(
    @SerializedName("name")
    val name: String,

    @SerializedName("number")
    val number: String,

    @SerializedName("month")
    val month: String,

    @SerializedName("year")
    val year: String,

    @SerializedName("cvc")
    val cvc: String,

    @SerializedName("manual")
    val manual: String?,

    @SerializedName("save_card")
    val saveCard: String?,

    @SerializedName("type")
    val type: String = "creditcard",

    @SerializedName("token")
    val token: String? = null,
) : PaymentSource

