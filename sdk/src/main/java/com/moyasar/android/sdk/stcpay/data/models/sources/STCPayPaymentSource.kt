package com.moyasar.android.sdk.stcpay.data.models.sources

import com.moyasar.android.sdk.core.data.PaymentSource
import com.google.gson.annotations.SerializedName

/**
 * Created by Mahmoud Ashraf on 22,September,2024
 */

data class STCPayPaymentSource(
    @SerializedName("mobile")
    val mobile: String,

    @SerializedName("type")
    val type: String = "stcpay"
) : PaymentSource
