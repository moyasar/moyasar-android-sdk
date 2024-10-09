package com.moyasar.android.sdk.stcpay.presentation.model.formatter

/**
 * Created by Mahmoud Ashraf on 22,September,2024
 */
internal object SaudiPhoneNumberFormatter {

    fun formatPhoneNumber(number: String): String {
        val cleanNumber = number.replace(" ", "")
        val segments = listOf(3, 3, 4)
        var formattedNumber = ""
        var startIndex = 0

        for ((index, segment) in segments.withIndex()) {
            val endIndex = (startIndex + segment).coerceAtMost(cleanNumber.length)
            val segmentString = cleanNumber.substring(startIndex, endIndex)
            formattedNumber += segmentString

            // Add a space if it's not the last segment
            if (endIndex < cleanNumber.length && index < segments.size - 1) {
                formattedNumber += " "
            }
            startIndex = endIndex
        }

        return formattedNumber
    }
}
