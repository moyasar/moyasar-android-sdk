package com.moyasar.android.sdk.payment.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
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
    val metadata: Map<String, String>?,
    val source: MutableMap<String, String>
) : Parcelable {
    fun getCardTransactionUrl(): String {
        if (!source.containsKey("type") || !source["type"].equals("creditcard")) {
            throw IllegalArgumentException("Source is not credit card")
        }

        return source["transaction_url"]!!
    }
    fun getStcPayTransactionUrl(): String {
        if (!source.containsKey("type") || !source["type"].equals("stcpay")) {
            throw IllegalArgumentException("Source is not stc pay")
        }

        return source["transaction_url"]!!
    }
}
