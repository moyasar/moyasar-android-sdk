package com.moyasar.android.sdk.core.util

internal object CreditCardFormatter {

  fun formatCardNumber(number: String): String {
    val cleaned = cleanNumber(number)
    return when {
      // AMEX: 4-6-5 (15 digits)
      cleaned.startsWith("34") || cleaned.startsWith("37") -> formatAMEXCardNumber(cleaned)
      // UnionPay: 4-4-4-4-{0-3} (16–19 digits)
      cleaned.startsWith("62") || cleaned.startsWith("60") || cleaned.startsWith("81") -> formatUnionPayCardNumber(cleaned)
      // All others (Visa, Mastercard, Mada): 4-4-4-4 (16 digits)
      else -> formatOtherCardNumber(cleaned)
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

  /// Format for UnionPay: xxxx xxxx xxxx xxxx{xxx} --> 4-4-4-4 up to 4-4-4-4-3 (16–19 digits)
  private fun formatUnionPayCardNumber(number: String): String {
    val maxLength = 19
    val truncated = number.take(maxLength)
    return truncated.chunked(4).joinToString(" ")
  }

  /// Format for other cards: xxxx xxxx xxxx xxxx --> 4-4-4-4
  private fun formatOtherCardNumber(number: String): String {
    val maxLength = 16
    val truncated = number.take(maxLength)
    return truncated.chunked(4).joinToString(" ")
  }

  internal fun cleanNumber(number: String): String {
    // This function should remove any non-numeric characters from the input number.
    // For now, it returns the input number unchanged.
    return number.filter { it.isDigit() }
  }
}