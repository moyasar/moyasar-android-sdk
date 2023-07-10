package com.moyasar.android.sdk.payment.models

data class StcPaymentSource(
    val mobile: String,
    val type: String = "stcpay",
) : PaymentSource
