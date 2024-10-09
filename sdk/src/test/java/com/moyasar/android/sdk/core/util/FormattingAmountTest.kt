package com.moyasar.android.sdk.core.util

import com.moyasar.android.sdk.creditcard.data.models.request.PaymentRequest
import org.junit.Assert.assertEquals
import org.junit.Test

class FormattingAmountTest {
  @Test
  fun `getFormattedAmount should format amount with currency symbol`() {
    val paymentRequest = PaymentRequest(currency = "SAR", amount = 1000, apiKey = "", description = "")
    val result = getFormattedAmount(paymentRequest)
    assertEquals("SAR 10.00", result)
  }
}