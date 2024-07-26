package com.moyasar.android.sdk.presentation.model


internal sealed class RequestResultViewState {
  data class Success<T>(val data: T) : RequestResultViewState()
  data class Failure(val e: Exception) : RequestResultViewState()
}