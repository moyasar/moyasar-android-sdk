package com.moyasar.android.sdk.util

import java.util.*

val amexRangeRegex = Regex("^3[47]")
val visaRangeRegex = Regex("^4")
val masterCardRangeRegex = Regex("^(?:5[1-5][0-9]{2}|222[1-9]|22[3-9][0-9]|2[3-6][0-9]{2}|27[01][0-9]|2720)")

fun isValidLuhnNumber(number: String): Boolean {
    var sum = number.replace(" ", "").last().digitToInt()

    for ((i, char) in number.toCharArray().withIndex()) {
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
    val number = number.replace(" ", "")
    return when {
        amexRangeRegex.matches(number) -> CreditCardNetwork.Amex
        inMadaRange(number) -> CreditCardNetwork.Mada
        visaRangeRegex.matches(number) -> CreditCardNetwork.Visa
        masterCardRangeRegex.matches(number) -> CreditCardNetwork.Mastercard
        else -> CreditCardNetwork.Unknown
    }
}

fun parseExpiry(date: String): ExpiryDate? {
    val clean = date.replace(" ", "")
        .replace("/", "")

    if (clean.length != 6) {
        return null
    }

    return ExpiryDate(clean.substring(0, 2).toInt(), clean.substring(2).toInt())
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
    fun isValid(): Boolean {
        return month in 1..12 && year > Calendar.getInstance().get(Calendar.YEAR)
    }

    fun expired(): Boolean {
        return Calendar.getInstance().before(expiryDate())
    }

    fun expiryDate(): Calendar {
        return Calendar.getInstance().apply {
            set(year, month, 1)
        }
    }
}
