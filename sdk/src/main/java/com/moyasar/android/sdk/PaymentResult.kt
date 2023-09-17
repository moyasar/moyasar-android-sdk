package com.moyasar.android.sdk

import android.os.Parcelable
import com.moyasar.android.sdk.payment.models.Payment
import com.moyasar.android.sdk.payment.models.Token
import kotlinx.parcelize.Parcelize

sealed class PaymentResult : Parcelable {
    @Parcelize
    data class Completed(val payment: Payment) : PaymentResult()

    @Parcelize
    data class CompletedToken(val token: Token) : PaymentResult()

    @Parcelize
    data class Failed(val error: Throwable) : PaymentResult()

    @Parcelize
    object Canceled : PaymentResult()
}
