package com.moyasar.android.sdk.stcpay.presentation.model.validator

/**
 * Created by Mahmoud Ashraf on 22,September,2024
 */
internal object STCPayOTPValidator {
    fun isValidOtp(otp: String): Boolean {
        return otp.length in 4..10
    }
}