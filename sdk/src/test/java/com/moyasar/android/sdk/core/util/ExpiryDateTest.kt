package com.moyasar.android.sdk.core.util

import junit.framework.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpiryDateTest {
  @Test
  fun `parseExpiry should return correct ExpiryDate for valid 4-digit format`() {
    val result = parseExpiry("1225")
    assertNotNull(result)
    assertEquals(12, result?.month)
    assertEquals(2025, result?.year)
  }

  @Test
  fun `parseExpiry should return correct ExpiryDate for valid 6-digit format`() {
    val result = parseExpiry("122025")
    assertNotNull(result)
    assertEquals(12, result?.month)
    assertEquals(2025, result?.year)
  }

  @Test
  fun `parseExpiry should return null for invalid length`() {
    val result = parseExpiry("12")
    assertNull(result)
  }

  @Test
  fun `parseExpiry should handle input with spaces and slashes`() {
    val result = parseExpiry("12 / 20")
    assertNotNull(result)
    assertEquals(12, result?.month)
    assertEquals(2020, result?.year)
  }

  @Test
  fun `parseExpiry should handle future expiry dates`() {
    val futureExpiry = ExpiryDate(12, 2025)
    assertFalse(futureExpiry.expired())
  }

  @Test
  fun `parseExpiry should handle past expiry dates`() {
    val pastExpiry = ExpiryDate(1, 2000)
    assertTrue(pastExpiry.expired())
  }

  @Test
  fun `parseExpiry should handle invalid dates`() {
    val invalidExpiry = ExpiryDate(13, 2025) // Invalid month
    assertTrue(invalidExpiry.isInvalid())
  }

  @Test
  fun `parseExpiry should handle invalid years`() {
    val invalidYearExpiry = ExpiryDate(12, 1899) // Invalid year
    assertTrue(invalidYearExpiry.isInvalid())
  }

}