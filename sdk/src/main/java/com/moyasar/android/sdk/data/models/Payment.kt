package com.moyasar.android.sdk.data.models

import com.google.gson.annotations.SerializedName

data class Payment(
    val id: String,
    var status: String,
    val amount: Int,
    val fee: Int,
    val currency: String,
    val refunded: Int,
    @SerializedName("refunded_at") val refundedAt: String?,
    val captured: Int,
    @SerializedName("captured_at") val capturedAt: String?,
    @SerializedName("voided_at") val voidedAt: String?,
    val description: String?,
    @SerializedName("invoice_id") val invoiceId: String?,
    val ip: String?,
    @SerializedName("callback_url") val callbackUrl: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val metadata: Map<String, Any>?,
    val source: MutableMap<String, String>
)  {
    fun getCardTransactionUrl(): String {
        if (!source.containsKey("type") || !source["type"].equals("creditcard")) {
            throw IllegalArgumentException("Source is not credit card")
        }

        return source["transaction_url"]!!
    }
}
