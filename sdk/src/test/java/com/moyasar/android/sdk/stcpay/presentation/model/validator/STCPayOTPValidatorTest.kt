package com.moyasar.android.sdk.stcpay.presentation.model.validator

import org.junit.Assert.*
import org.junit.Test

/**
 * Created by Mahmoud Ashraf on 29,September,2024
 */

class STCPayOTPValidatorTest {

    @Test
    fun `valid OTP with minimum length`() {
        val validOtp = "1234" // OTP of length 4
        val result = STCPayOTPValidator.isValidOtp(validOtp)
        assertTrue(result)
    }

    @Test
    fun `valid OTP with maximum length`() {
        val validOtp = "1234567890" // OTP of length 10
        val result = STCPayOTPValidator.isValidOtp(validOtp)
        assertTrue(result)
    }

    @Test
    fun `invalid OTP with less than minimum length`() {
        val invalidOtp = "123" // OTP of length 3
        val result = STCPayOTPValidator.isValidOtp(invalidOtp)
        assertFalse(result)
    }

    @Test
    fun `invalid OTP with more than maximum length`() {
        val invalidOtp = "12345678901" // OTP of length 11
        val result = STCPayOTPValidator.isValidOtp(invalidOtp)
        assertFalse(result)
    }

    @Test
    fun `valid OTP within range`() {
        val validOtp = "1234567" // OTP of length 7
        val result = STCPayOTPValidator.isValidOtp(validOtp)
        assertTrue(result)
    }

    @Test
    fun `invalid OTP with empty string`() {
        val invalidOtp = "" // Empty OTP
        val result = STCPayOTPValidator.isValidOtp(invalidOtp)
        assertFalse(result)
    }
}
