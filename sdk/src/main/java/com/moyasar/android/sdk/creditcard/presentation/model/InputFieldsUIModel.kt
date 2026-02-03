package com.moyasar.android.sdk.creditcard.presentation.model

import com.moyasar.android.sdk.core.util.ExpiryDate

/**
 * Created by Mahmoud Ashraf on 06,May,2025
 */
data class InputFieldsUIModel(
    val name: String = "",
    val cardNumber: String = "",
    val expiryDate: String = "",
    val cvc: String = "",
    val errorMessage: FormErrorMessage? = null,
    val stcPayUIModel: STCPayUIModel? = null,
    val isFormValid: Boolean = false,
)

data class STCPayUIModel(
    val mobileNumber: String = "",
    val mobileNumberErrorMsg: String = "",
    val isMobileValid: Boolean = false,
    val isOTPValid: Boolean = false,
    val otp: String = "",
    val otpErrorMsg: String = "",
)

data class FormErrorMessage(
    val nameErrorMsg: String = "",
    val numberErrorMsg: String = "",
    val cvcErrorMsg: String = "",
    val expiryDateErrorMsg: String = "",
)
