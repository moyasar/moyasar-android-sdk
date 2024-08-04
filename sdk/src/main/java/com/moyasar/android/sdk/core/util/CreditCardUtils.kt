package com.moyasar.android.sdk.core.util

import com.moyasar.android.sdk.presentation.model.PaymentConfig
import java.text.DecimalFormat
import java.util.Calendar
import java.util.Currency
import java.util.Locale
import kotlin.math.pow

val amexRangeRegex = Regex("^3[47]")
val visaRangeRegex = Regex("^4")
val masterCardRangeRegex =
  Regex("^(?:5[1-5][0-9]{2}|222[1-9]|22[3-9][0-9]|2[3-6][0-9]{2}|27[01][0-9]|2720)")

fun isValidLuhnNumber(number: String): Boolean {
  val cleanNumber = number.replace(" ", "")
  if (!cleanNumber.all { it.isDigit() }) {
    return false
  }

  val doubleSum = intArrayOf(0, 2, 4, 6, 8, 1, 3, 5, 7, 9)
  var sum = 0

  for ((index, char) in cleanNumber.reversed().withIndex()) {
    val digit = char.toString().toIntOrNull() ?: return false
    sum += if (index % 2 == 0) digit else doubleSum[digit]
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

fun getFormattedAmount(paymentConfig: PaymentConfig): String {
  val currentLocale = Locale.getDefault()
  val paymentCurrency = Currency.getInstance(paymentConfig.currency)

  val numberFormatter = DecimalFormat.getNumberInstance(Locale.US).apply {
    minimumFractionDigits = paymentCurrency.defaultFractionDigits
    isGroupingUsed = true
  }

  val currencyFormatter = DecimalFormat.getCurrencyInstance(currentLocale).apply {
    currency = paymentCurrency
  }

  val amount =
    paymentConfig.amount / (10.0.pow(currencyFormatter.currency!!.defaultFractionDigits.toDouble()))
  val formattedNumber = numberFormatter.format(amount)
  val currencySymbol = currencyFormatter.currency!!.symbol

  return if (currentLocale.language == "ar") {
    "$formattedNumber $currencySymbol"
  } else {
    "$currencySymbol $formattedNumber"
  }
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
