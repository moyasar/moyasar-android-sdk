package com.moyasar.android.sdk.payment.models

data class CardPaymentSource(
    val name: String,
    val number: String,
    val month: String,
    val year: String,
    val cvc: String,
    val type: String = "creditcard",
) : PaymentSource
