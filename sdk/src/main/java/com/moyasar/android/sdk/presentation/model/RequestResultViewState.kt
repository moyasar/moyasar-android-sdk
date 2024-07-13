package com.moyasar.android.sdk.presentation.model

import com.moyasar.android.sdk.data.models.Payment


internal sealed class RequestResultViewState {
  data class Success(val payment: Payment) : RequestResultViewState()
  data class Failure(val e: Exception) : RequestResultViewState()
}