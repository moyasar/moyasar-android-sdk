package com.moyasar.android.sdk.core.util

import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class LuhnNumberTest {

  @Test
  fun testValidLuhnNumbers() {
    // Valid Luhn numbers (test with different formats)
    val validNumbers = listOf(
      "4539 1488 0343 6467", // Standard format
      "4539148803436467",    // No spaces
      "6011 1111 1111 1117", // Another valid number
      "378282246310005"      // American Express format
    )

    for (number in validNumbers) {
      assertTrue("$number should be a valid Luhn number", isValidLuhnNumber(number))
    }
  }

  @Test
  fun testInvalidLuhnNumbers() {
    // Invalid Luhn numbers (test with different formats)
    val invalidNumbers = listOf(
      "4539 1488 0343 6466",
      "6011 1111 1111 1112",
      "378282246310006",
      "123456789012345"
    )

    for (number in invalidNumbers) {
      assertFalse("$number should not be a valid Luhn number", isValidLuhnNumber(number))
    }
  }

  @Test
  fun testNonNumericInput() {
    // Non-numeric input
    val nonNumericInputs = listOf(
      "4539 1488 0343 ABCD", // Contains letters
      "12 34 56 78",          // Contains spaces but not digits
      "4539 14@8 0343 6467"   // Contains special characters
    )

    for (input in nonNumericInputs) {
      assertFalse("$input should not be a valid Luhn number", isValidLuhnNumber(input))
    }
  }
}