package com.moyasar.android.sdk.stcpay.presentation.model.validator

/**
 * Created by Mahmoud Ashraf on 22,September,2024
 */
internal object SaudiPhoneNumberValidator {

    fun isValidSaudiPhoneNumber(phoneNumber: String): Boolean {
        val cleanPhoneNumber = phoneNumber.replace(" ", "")
        val pattern = Regex("^05[0-9]{8}$")
        return pattern.matches(cleanPhoneNumber)
    }
}