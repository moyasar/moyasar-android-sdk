package com.moyasar.android.sdk.stcpay.presentation.model.formatter

import org.junit.Assert.*
import org.junit.Test

/**
 * Created by Mahmoud Ashraf on 29,September,2024
 */
class SaudiPhoneNumberFormatterTest {

    @Test
    fun `test formatPhoneNumber with valid Saudi number`() {
        // Given a valid Saudi phone number
        val phoneNumber = "0123456789"

        // When formatting the phone number
        val formattedNumber = SaudiPhoneNumberFormatter.formatPhoneNumber(phoneNumber)

        // Then the formatted number should match the expected format
        val expected = "012 345 6789"
        assertEquals(expected, formattedNumber)
    }

    @Test
    fun `test formatPhoneNumber with already formatted number`() {
        // Given a phone number that is already formatted
        val phoneNumber = "012 345 6789"

        // When formatting the phone number
        val formattedNumber = SaudiPhoneNumberFormatter.formatPhoneNumber(phoneNumber)

        // Then the formatted number should stay the same
        val expected = "012 345 6789"
        assertEquals(expected, formattedNumber)
    }

    @Test
    fun `test formatPhoneNumber with number containing spaces`() {
        // Given a phone number with spaces
        val phoneNumber = "012  345  6789"

        // When formatting the phone number
        val formattedNumber = SaudiPhoneNumberFormatter.formatPhoneNumber(phoneNumber)

        // Then the formatted number should match the expected format without extra spaces
        val expected = "012 345 6789"
        assertEquals(expected, formattedNumber)
    }

    @Test
    fun `test formatPhoneNumber with shorter number`() {
        // Given a phone number that is too short
        val phoneNumber = "01234"

        // When formatting the phone number
        val formattedNumber = SaudiPhoneNumberFormatter.formatPhoneNumber(phoneNumber)

        // Then the formatted number should be formatted as far as possible
        val expected = "012 34"
        assertEquals(expected, formattedNumber)
    }

    @Test
    fun `test formatPhoneNumber with empty string`() {
        // Given an empty phone number
        val phoneNumber = ""

        // When formatting the phone number
        val formattedNumber = SaudiPhoneNumberFormatter.formatPhoneNumber(phoneNumber)

        // Then the formatted number should be empty
        val expected = ""
        assertEquals(expected, formattedNumber)
    }
}