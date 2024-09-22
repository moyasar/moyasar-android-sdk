package com.moyasar.android.sdk.creditcard.data.models.request

import com.google.gson.annotations.SerializedName

data class TokenRequest(
    val name: String,
    val number: String,
    val cvc: String?,
    val month: String?,
    val year: String?,
    @SerializedName("save_only") val saveOnly: Boolean = false,
    @SerializedName("callback_url") val callbackUrl: String?,
    val metadata: Map<String, Any> = HashMap()
)
