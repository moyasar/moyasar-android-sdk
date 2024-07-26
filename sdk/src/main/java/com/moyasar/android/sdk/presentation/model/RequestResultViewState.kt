package com.moyasar.android.sdk.presentation.model


internal sealed class RequestResultViewState<out T>{
  data class Success<out T>(val data: T) : RequestResultViewState<T>()
  data class Failure(val e: Exception) : RequestResultViewState<Nothing>()
}