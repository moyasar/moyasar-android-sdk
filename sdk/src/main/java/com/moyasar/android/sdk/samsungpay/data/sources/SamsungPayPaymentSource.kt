package com.moyasar.android.sdk.samsungpay.data.sources

import com.moyasar.android.sdk.core.data.PaymentSource

data class SamsungPayPaymentSource(
    val type: String = "samsungpay",
    val token: String,
    val manual: String = "false"
) : PaymentSource
