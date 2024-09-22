package com.moyasar.android.sdk.creditcard.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class PaymentStatusViewState : Parcelable {
  @Parcelize
  data object Reset : PaymentStatusViewState()

  @Parcelize
  data object SubmittingPayment : PaymentStatusViewState()

  @Parcelize
  data class PaymentAuth3dSecure(val url: String) : PaymentStatusViewState()

  @Parcelize
  data object SubmittingSTCPayMobileNumber : PaymentStatusViewState()

  @Parcelize
  data object SubmittingSTCPayOTP : PaymentStatusViewState()

  @Parcelize
  data class STCPayOTPAuth(val url: String) : PaymentStatusViewState()
}