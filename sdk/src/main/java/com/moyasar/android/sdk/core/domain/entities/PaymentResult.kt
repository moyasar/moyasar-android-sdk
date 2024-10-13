package com.moyasar.android.sdk.core.domain.entities

import com.moyasar.android.sdk.core.data.response.PaymentResponse
import com.moyasar.android.sdk.creditcard.data.models.response.TokenResponse

sealed class PaymentResult {
    data class Completed(val payment: PaymentResponse) : PaymentResult()

    data class CompletedToken(val token: TokenResponse) : PaymentResult()

    data class Failed(val error: Throwable) : PaymentResult()

    data object Canceled : PaymentResult()
}
