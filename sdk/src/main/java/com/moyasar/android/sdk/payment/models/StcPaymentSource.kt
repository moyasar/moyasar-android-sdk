package com.moyasar.android.sdk.payment.models

data class StcPaymentSource(
    val number: String,
    val type: String = "stcpay",
) : PaymentSource
