package com.moyasar.android.sdk.creditcard.presentation.model

data class InputCreditCardUIModel(
    val numberHint: String,
    val expiryDateHint: String,
    val cvcHint: String,
    val numberType: Int,
    val expiryDateType: Int,
    val cvcType: Int,
)
