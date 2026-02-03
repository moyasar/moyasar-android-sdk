package com.moyasar.android.sdk.core.data.response

import com.google.gson.annotations.SerializedName
import com.moyasar.android.sdk.stcpay.data.constants.STC_PAY_TYPE
import kotlinx.parcelize.RawValue

data class PaymentResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("status")
    var status: String,

    @SerializedName("amount")
    val amount: Int,

    @SerializedName("fee")
    val fee: Int,

    @SerializedName("currency")
    val currency: String,

    @SerializedName("refunded")
    val refunded: Int,

    @SerializedName("refunded_at")
    val refundedAt: String?,

    @SerializedName("captured")
    val captured: Int,

    @SerializedName("captured_at")
    val capturedAt: String?,

    @SerializedName("voided_at")
    val voidedAt: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("invoice_id")
    val invoiceId: String?,

    @SerializedName("ip")
    val ip: String?,

    @SerializedName("callback_url")
    val callbackUrl: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("metadata")
    val metadata: Map<String,@RawValue Any>?,

    @SerializedName("source")
    val source: MutableMap<String, String>
) {
    fun getCardTransactionUrl(): String {
        if (!source.containsKey("type") || !source["type"].equals("creditcard")) {
            throw IllegalArgumentException("Source is not credit card")
        }

        return source["transaction_url"]!!
    }

    fun getSTCPayTransactionUrl(): String {
        if (!source.containsKey("type") || !source["type"].equals(STC_PAY_TYPE)) {
            throw IllegalArgumentException("Source is not STC Pay")
        }

        return source["transaction_url"]!!
    }
}
