package com.moyasar.android.sdk.creditcard.data.models

import com.moyasar.android.sdk.core.util.amexRangeRegex
import com.moyasar.android.sdk.core.util.inMadaRange
import com.moyasar.android.sdk.core.util.masterCardRangeRegex
import com.moyasar.android.sdk.core.util.visaRangeRegex

/**
 * Created by Mahmoud Ashraf on 01,October,2024
 */
enum class CreditCardNetwork {
    Amex,
    Mada,
    Visa,
    Mastercard,
    Unknown
}

fun getNetwork(number: String): CreditCardNetwork {
    val strippedNumber = number.replace(" ", "")
    return when {
        amexRangeRegex.containsMatchIn(strippedNumber) -> CreditCardNetwork.Amex
        inMadaRange(strippedNumber) -> CreditCardNetwork.Mada
        visaRangeRegex.containsMatchIn(strippedNumber) -> CreditCardNetwork.Visa
        masterCardRangeRegex.containsMatchIn(strippedNumber) -> CreditCardNetwork.Mastercard
        else -> CreditCardNetwork.Unknown
    }
}

fun isCreditAllowed(number: String, allowedNetwork: List<CreditCardNetwork>): Boolean {
    val strippedNumber = number.replace(" ", "")
    return when {
        amexRangeRegex.containsMatchIn(strippedNumber) && allowedNetwork.any { it.name == CreditCardNetwork.Amex.name } -> true
        inMadaRange(strippedNumber)  && allowedNetwork.any { it.name == CreditCardNetwork.Mada.name } -> true
        visaRangeRegex.containsMatchIn(strippedNumber)  && allowedNetwork.any { it.name == CreditCardNetwork.Visa.name }  -> true
        masterCardRangeRegex.containsMatchIn(strippedNumber)  && allowedNetwork.any { it.name == CreditCardNetwork.Mastercard.name } -> true
        else -> false
    }
}