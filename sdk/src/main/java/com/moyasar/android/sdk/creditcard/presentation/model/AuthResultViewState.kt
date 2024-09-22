package com.moyasar.android.sdk.creditcard.presentation.model


sealed class AuthResultViewState {
  data class Completed(val id: String, val status: String, val message: String) :
    AuthResultViewState()

  data class Failed(val error: String? = null) : AuthResultViewState()

  data object Canceled : AuthResultViewState()
}