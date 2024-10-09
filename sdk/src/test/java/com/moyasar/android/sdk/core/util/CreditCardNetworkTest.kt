package com.moyasar.android.sdk.core.util

import com.moyasar.android.sdk.creditcard.data.models.CreditCardNetwork
import com.moyasar.android.sdk.creditcard.data.models.getNetwork
import junit.framework.TestCase.assertEquals
import org.junit.Test

class CreditCardNetworkTest {
  @Test
  fun `should return Amex for Amex number`() {
    val number = "341234567890123"
    val result = getNetwork(number)
    assertEquals(CreditCardNetwork.Amex, result)
  }

  @Test
  fun `should return Visa for Visa number`() {
    val number = "4123456789012345"
    val result = getNetwork(number)
    assertEquals(CreditCardNetwork.Visa, result)
  }

  @Test
  fun `should return Mastercard for Mastercard number`() {
    val number = "5212345678901234"
    val result = getNetwork(number)
    assertEquals(CreditCardNetwork.Mastercard, result)
  }

  @Test
  fun `should return Unknown for number that doesn't match any pattern`() {
    val number = "6012345678901234"
    val result = getNetwork(number)
    assertEquals(CreditCardNetwork.Unknown, result)
  }

  @Test
  fun `should return Mada for Mada number`() {
    val number = "4830104000000001"
    val result = getNetwork(number)
    assertEquals(CreditCardNetwork.Mada, result)
  }

  @Test
  fun `should return Amex for Amex number with spaces`() {
    val number = "34 1234 5678 9012 34"
    val result = getNetwork(number)
    assertEquals(CreditCardNetwork.Amex, result)
  }

  @Test
  fun `should return Visa for Visa number with spaces`() {
    val number = "4 1234 5678 9012 3456"
    val result = getNetwork(number)
    assertEquals(CreditCardNetwork.Visa, result)
  }

  @Test
  fun `should return Mastercard for Mastercard number with spaces`() {
    val number = "5 1234 5678 9012 3456"
    val result = getNetwork(number)
    assertEquals(CreditCardNetwork.Mastercard, result)
  }

  @Test
  fun `should return Unknown for number with special characters`() {
    val number = "5#12#34#5678#9012#3456"
    val result = getNetwork(number)
    assertEquals(CreditCardNetwork.Unknown, result)
  }

}