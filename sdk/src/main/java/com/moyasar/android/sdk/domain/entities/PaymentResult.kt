package com.moyasar.android.sdk.domain.entities

import android.os.Parcelable
import com.moyasar.android.sdk.data.models.Payment
import com.moyasar.android.sdk.data.models.Token
import kotlinx.parcelize.Parcelize

sealed class PaymentResult : Parcelable {
    @Parcelize
    data class Completed(val payment: Payment) : PaymentResult()

    @Parcelize
    data class CompletedToken(val token: Token) : PaymentResult()

    @Parcelize
    data class Failed(val error: Throwable) : PaymentResult()

    @Parcelize
    data object Canceled : PaymentResult()
}
