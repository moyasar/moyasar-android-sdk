package com.moyasar.android.sdk.creditcard.data.models.request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PaymentSplit(
    @SerializedName("recipient_id")
    val recipientId: String,
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("reference")
    val reference: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("fee_source")
    val feeSource: Boolean? = null,
    @SerializedName("refundable")
    val refundable: Boolean? = null
)


