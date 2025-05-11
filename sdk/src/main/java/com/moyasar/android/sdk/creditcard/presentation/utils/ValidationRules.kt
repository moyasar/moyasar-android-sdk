package com.moyasar.android.sdk.creditcard.presentation.utils

import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.core.domain.entities.ValidationRule
import com.moyasar.android.sdk.core.util.cleanSpaces
import com.moyasar.android.sdk.core.util.isValidLuhnNumber
import com.moyasar.android.sdk.core.util.parseExpiry
import com.moyasar.android.sdk.creditcard.data.models.CreditCardNetwork
import com.moyasar.android.sdk.creditcard.data.models.getNetwork
import com.moyasar.android.sdk.creditcard.data.models.isCreditAllowed
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer.application
import com.moyasar.android.sdk.stcpay.presentation.model.validator.STCPayOTPValidator
import com.moyasar.android.sdk.stcpay.presentation.model.validator.SaudiPhoneNumberValidator

/**
 * Created by Mahmoud Ashraf on 11,May,2025
 */

internal fun getNameValidationRules(): MutableList<ValidationRule> {
    val rules = mutableListOf<ValidationRule>()
    val latinRegex = Regex("^[a-zA-Z\\-\\s]+\$")
    val nameRegex = Regex("^[a-zA-Z\\-]+\\s+?([a-zA-Z\\-]+\\s?)+\$")

    rules.add(
        ValidationRule(
            { it.isNullOrBlank() },
            application.getString(R.string.name_is_required)
        )
    )
    rules.add(ValidationRule({
        !latinRegex.matches(
            it ?: ""
        )
    }, application.getString(R.string.only_english_alpha)))

    rules.add(ValidationRule({
        !nameRegex.matches(
            it ?: ""
        )
    }, application.getString(R.string.both_names_required)))
    return rules
}

internal fun getNumberValidationRules(): MutableList<ValidationRule> {
    val rules = mutableListOf<ValidationRule>()
    rules.add(
        ValidationRule(
            { it.isNullOrBlank() },
            application.getString(R.string.card_number_required)
        )
    )
    rules.add(ValidationRule({
        !isValidLuhnNumber(
            it ?: ""
        ) || it?.cleanSpaces().orEmpty().length < 15
    }, application.getString(R.string.invalid_card_number)))
    rules.add(ValidationRule({
        getNetwork(
            number = it ?: ""
        ) == CreditCardNetwork.Unknown || !isCreditAllowed(
            number = it ?: "",
            allowedNetwork = MoyasarAppContainer.paymentRequest.allowedNetworks
        )
    }, application.getString(R.string.unsupported_network)))

    return rules
}

internal fun getCvcValidationRules(number: String): MutableList<ValidationRule> {
    val rules = mutableListOf<ValidationRule>()
    rules.add(
        ValidationRule(
            { it.isNullOrBlank() },
            application.getString(R.string.cvc_required)
        )
    )
    rules.add(ValidationRule({
        when (getNetwork(
            number = number
        )) {
            CreditCardNetwork.Amex -> (it?.length ?: 0) < 4
            else -> (it?.length ?: 0) < 3
        }
    }, application.getString(R.string.invalid_cvc)))
    return rules
}

internal fun getExpiryDateValidationRules(): MutableList<ValidationRule> {
    val rules = mutableListOf<ValidationRule>()
    rules.add(
        ValidationRule(
            { it.isNullOrBlank() },
            application.getString(R.string.expiry_is_required)
        )
    )
    rules.add(ValidationRule({
        parseExpiry(it ?: "")?.isInvalid() ?: true
    }, application.getString(R.string.invalid_expiry)))
    rules.add(ValidationRule({
        parseExpiry(it ?: "")?.expired() ?: false
    }, application.getString(R.string.expired_card)))
    return rules
}

internal fun getPhoneNumberValidationRules(): MutableList<ValidationRule> {
    val rules = mutableListOf<ValidationRule>()
    rules.add(
        ValidationRule(
            { it.isNullOrBlank() },
            application.getString(R.string.mobile_number_is_required)
        )
    )
    rules.add(
        ValidationRule(
            {
                !SaudiPhoneNumberValidator.isValidSaudiPhoneNumber(
                    it.orEmpty()
                )
            },
            application.getString(R.string.invalid_mobile_number)
        )
    )
    return rules
}

internal fun getOTPValidationRules(): MutableList<ValidationRule> {
    val rules = mutableListOf<ValidationRule>()
    rules.add(
        ValidationRule(
            {
                !STCPayOTPValidator.isValidOtp(
                    it ?: ""
                )
            },
            application.getString(R.string.invalid_stc_pay_otp)
        )
    )

    return rules
}
