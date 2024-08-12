package com.moyasar.android.sdk.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CreditCardFormatterTest {
  @Test
  fun testFormatCardNumber_Amex_CorrectFormatting_startWith_34() {
    val result = CreditCardFormatter.formatCardNumber("341234567890123")
    assertEquals("3412 345678 90123", result)
  }

  @Test
  fun testFormatCardNumber_Amex_CorrectFormatting_startWith_37() {
    val result = CreditCardFormatter.formatCardNumber("371234567890123")
    assertEquals("3712 345678 90123", result)
  }

  @Test
  fun testFormatCardNumber_Amex_WithSpaces() {
    val result = CreditCardFormatter.formatCardNumber("34 12 34 56 78 90 12 3")
    assertEquals("3412 345678 90123", result)
  }

  @Test
  fun testFormatCardNumber_Amex_EmptyInput() {
    val result = CreditCardFormatter.formatCardNumber("")
    assertEquals("", result)
  }

  @Test
  fun testFormatCardNumber_OtherCard_CorrectFormatting() {
    val result = CreditCardFormatter.formatCardNumber("1234567812345678")
    assertEquals("1234 5678 1234 5678", result)
  }

  @Test
  fun testFormatCardNumber_OtherCard_WithSpaces() {
    val result = CreditCardFormatter.formatCardNumber("1234 5678 1234 5678")
    assertEquals("1234 5678 1234 5678", result)
  }

  @Test
  fun testFormatCardNumber_OtherCard_EmptyInput() {
    val result = CreditCardFormatter.formatCardNumber("")
    assertEquals("", result)
  }

  @Test
  fun testFormatCardNumber_OtherCard_TooShort() {
    val result = CreditCardFormatter.formatCardNumber("1234")
    assertEquals("1234", result)
  }

  @Test
  fun testCleanNumber_RemovesNonDigits() {
    val result = CreditCardFormatter.cleanNumber("12-34 56/78")
    assertEquals("12345678", result)
  }

  @Test
  fun testCleanNumber_EmptyInput() {
    val result = CreditCardFormatter.cleanNumber("")
    assertEquals("", result)
  }

  @Test
  fun testCleanNumber_NoNonDigits() {
    val result = CreditCardFormatter.cleanNumber("123456")
    assertEquals("123456", result)
  }

}