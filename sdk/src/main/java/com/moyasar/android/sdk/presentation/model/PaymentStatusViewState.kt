package com.moyasar.android.sdk.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class PaymentStatusViewState : Parcelable {
  @Parcelize
  data object Reset : PaymentStatusViewState()

  @Parcelize
  data object SubmittingPayment : PaymentStatusViewState()

  @Parcelize
  data class PaymentAuth3dSecure(val url: String) : PaymentStatusViewState()
}