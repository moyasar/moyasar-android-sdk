package com.moyasar.android.sdk.core.util

import com.moyasar.android.sdk.creditcard.presentation.model.PaymentConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class FormattingAmountTest {
  @Test
  fun `getFormattedAmount should format amount with currency symbol`() {
    val paymentConfig = PaymentConfig(currency = "SAR", amount = 1000, apiKey = "", description = "")
    val result = getFormattedAmount(paymentConfig)
    assertEquals("SAR 10.00", result)
  }
}