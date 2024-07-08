package com.moyasar.android.sdk.data.models

import com.google.gson.annotations.SerializedName

data class PaymentRequest(
  val amount: Int,
  val currency: String,
  val description: String,
  @SerializedName("callback_url") val callbackUrl: String,
  val source: PaymentSource,
  val metadata: Map<String, String> = HashMap()
)
