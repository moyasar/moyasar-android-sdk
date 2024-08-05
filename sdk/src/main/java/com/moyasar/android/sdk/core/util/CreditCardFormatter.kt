package com.moyasar.android.sdk.core.util


internal object CreditCardFormatter {

  fun formatCardNumber(number: String): String {
    val cleaned = cleanNumber(number)
    // Check if the card is AMEX based on the starting digits
    val isAMEX = cleaned.startsWith("34") || cleaned.startsWith("37")
    return if (isAMEX) {
      formatAMEXCardNumber(cleaned)
    } else {
      formatOtherCardNumber(cleaned)
    }
  }

  /// Format for AMEX: xxxx xxxxxx xxxxx  -->  4-6-5
  private fun formatAMEXCardNumber(number: String): String {
    val segments = listOf(4, 6, 5)
    val formattedNumber = StringBuilder()
    var startIndex = 0
    for (segment in segments) {
      val endIndex = (startIndex + segment).coerceAtMost(number.length)
      val segmentString = number.substring(startIndex, endIndex)
      formattedNumber.append(segmentString)
      if (endIndex < number.length && cleanNumber(formattedNumber.toString()).length != 15) {
        formattedNumber.append(" ")
      }
      startIndex = endIndex
    }
    return formattedNumber.toString()
  }

  /// Format for other cards: xxxx xxxx xxxx xxxx --> 4-4-4-4
  private fun formatOtherCardNumber(number: String): String {
    val maxLength = 16
    val truncated = number.take(maxLength)
    return truncated.chunked(4).joinToString(" ")
  }

  private fun cleanNumber(number: String): String {
    // This function should remove any non-numeric characters from the input number.
    // For now, it returns the input number unchanged.
    return number.filter { it.isDigit() }
  }
}