package com.moyasar.android.sdk.stcpay.presentation.model.validator

import org.junit.Assert.*
import org.junit.Test

/**
 * Created by Mahmoud Ashraf on 29,September,2024
 */
class SaudiPhoneNumberValidatorTest {

    @Test
    fun `valid Saudi phone number`() {
        val validPhoneNumber = "0551234567"
        val result = SaudiPhoneNumberValidator.isValidSaudiPhoneNumber(validPhoneNumber)
        assertTrue(result)
    }

    @Test
    fun `valid Saudi phone number with spaces`() {
        val validPhoneNumberWithSpaces = "055 123 4567"
        val result = SaudiPhoneNumberValidator.isValidSaudiPhoneNumber(validPhoneNumberWithSpaces)
        assertTrue(result)
    }

    @Test
    fun `invalid Saudi phone number with less digits`() {
        val invalidPhoneNumber = "05512345"
        val result = SaudiPhoneNumberValidator.isValidSaudiPhoneNumber(invalidPhoneNumber)
        assertFalse(result)
    }

    @Test
    fun `invalid Saudi phone number with more digits`() {
        val invalidPhoneNumber = "055123456789"
        val result = SaudiPhoneNumberValidator.isValidSaudiPhoneNumber(invalidPhoneNumber)
        assertFalse(result)
    }

    @Test
    fun `invalid Saudi phone number with wrong prefix`() {
        val invalidPhoneNumber = "0651234567"
        val result = SaudiPhoneNumberValidator.isValidSaudiPhoneNumber(invalidPhoneNumber)
        assertFalse(result)
    }

    @Test
    fun `invalid Saudi phone number with non-numeric characters`() {
        val invalidPhoneNumber = "05512345AB"
        val result = SaudiPhoneNumberValidator.isValidSaudiPhoneNumber(invalidPhoneNumber)
        assertFalse(result)
    }
}
