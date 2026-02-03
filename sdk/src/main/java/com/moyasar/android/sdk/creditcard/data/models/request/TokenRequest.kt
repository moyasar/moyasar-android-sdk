package com.moyasar.android.sdk.creditcard.data.models.request

import com.google.gson.annotations.SerializedName

data class TokenRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("number")
    val number: String,

    @SerializedName("cvc")
    val cvc: String?,

    @SerializedName("month")
    val month: String?,

    @SerializedName("year")
    val year: String?,

    @SerializedName("save_only")
    val saveOnly: Boolean = false,

    @SerializedName("callback_url")
    val callbackUrl: String?,

    @SerializedName("metadata")
    val metadata: Map<String, Any> = HashMap()
)
