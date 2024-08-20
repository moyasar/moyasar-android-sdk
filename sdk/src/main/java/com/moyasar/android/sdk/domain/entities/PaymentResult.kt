package com.moyasar.android.sdk.domain.entities

import com.moyasar.android.sdk.data.models.Payment
import com.moyasar.android.sdk.data.models.Token

sealed class PaymentResult {
    data class Completed(val payment: Payment) : PaymentResult()

    data class CompletedToken(val token: Token) : PaymentResult()

    data class Failed(val error: Throwable) : PaymentResult()

    data object Canceled : PaymentResult()
}
