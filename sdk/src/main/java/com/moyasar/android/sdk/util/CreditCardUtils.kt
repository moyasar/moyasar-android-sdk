package com.moyasar.android.sdk.util

import java.util.Calendar

val amexRangeRegex = Regex("^3[47]")
val visaRangeRegex = Regex("^4")
val masterCardRangeRegex = Regex("^(?:5[1-5][0-9]{2}|222[1-9]|22[3-9][0-9]|2[3-6][0-9]{2}|27[01][0-9]|2720)")

fun isValidLuhnNumber(number: String): Boolean {
    val cleanNumber = number.replace(" ", "")
    var sum = cleanNumber.last().digitToInt()

    for ((i, char) in cleanNumber.toCharArray().take(cleanNumber.length - 1).withIndex()) {
        var value = char.digitToInt()
        if (i % 2 == 0) {
            value *= 2
        }
        if (value > 9) {
            value -= 9
        }
        sum += value
    }

    return sum % 10 == 0
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

fun parseExpiry(date: String): ExpiryDate? {
    val clean = date.replace(" ", "")
        .replace("/", "")

    return when (clean.length) {
        4 -> {
            val millennium = (Calendar.getInstance().get(Calendar.YEAR) / 100) * 100
            ExpiryDate(clean.substring(0, 2).toInt(), millennium + clean.substring(2).toInt())
        }
        6 -> {
            ExpiryDate(clean.substring(0, 2).toInt(), clean.substring(2).toInt())
        }
        else -> null
    }
}

fun isValidCvc(network: CreditCardNetwork, cvc: String): Boolean {
    val digits = when (network) {
        CreditCardNetwork.Amex -> 4
        else -> 3
    }
    return cvc.length == digits
}

enum class CreditCardNetwork {
    Amex,
    Mada,
    Visa,
    Mastercard,
    Unknown
}

data class ExpiryDate(val month: Int, val year: Int) {
    private fun isValid(): Boolean {
        return month in 1..12 && year > 1900
    }

    private fun expiryDate(): Calendar {
        return Calendar.getInstance().apply {
            set(year, month, 1)
        }
    }

    fun isInvalid(): Boolean {
        return !isValid()
    }

    fun expired(): Boolean {
        return Calendar.getInstance().after(expiryDate())
    }
}
