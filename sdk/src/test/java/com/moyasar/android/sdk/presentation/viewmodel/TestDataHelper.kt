package com.moyasar.android.sdk.presentation.viewmodel

import com.moyasar.android.sdk.data.models.CardPaymentSource
import com.moyasar.android.sdk.data.models.Payment
import com.moyasar.android.sdk.data.models.PaymentRequest
import com.moyasar.android.sdk.data.models.Token
import com.moyasar.android.sdk.data.models.TokenRequest
import com.moyasar.android.sdk.presentation.model.PaymentConfig
import com.moyasar.android.sdk.presentation.view.fragments.PaymentAuthFragment

/**
 * Created by Mahmoud Ashraf on 18,August,2024
 */
object TestDataHelper {
  internal fun createPaymentRequestBody() = PaymentRequest(
    amount = 100000,
    currency = "SAR",
    description = "Sample Android SDK Payment",
    callbackUrl = PaymentAuthFragment.RETURN_URL,
    source = CardPaymentSource(
      name = "John Doe",
      number = "4111111111111111",
      month = "12",
      year = "2025",
      cvc = "123",
      manual = "false",
      saveCard = "false",
      type = "creditcard",
    ),
    metadata = mapOf("order_id" to "order_123")
  )

  internal fun getPaymentBody() = Payment(
    "1",
    "initiated",
    1000,
    0,
    "SAR",
    0,
    "",
    0,
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    mapOf(),
    mutableMapOf("type" to "creditcard", "transaction_url" to "http://example.com")
  )

  internal fun getPaymentConfig() = PaymentConfig(
    amount = 100000,
    currency = "SAR",
    description = "Sample Android SDK Payment",
    apiKey = "pk_test_vcFUHJDBwiyRu4Bd3hFuPpTnRPY4gp2ssYdNJMY3",
    baseUrl = "https://api.moyasar.com",
    manual = false,
    metadata = mapOf(
      "order_id" to "order_123"
    ),
    createSaveOnlyToken = false
  )

  internal fun createTokenRequestBody() = TokenRequest(
    name = "asd",
    number = "1234",
    cvc = "123",
    month = "11",
    year = "2024",
    saveOnly = true,
    callbackUrl = "",
    metadata = mapOf()
  )

  internal fun getTokenResponseBody() = Token(
    id = "1",
    status = "success",
    brand = "",
    funding = "",
    country = "",
    month = "",
    year = "",
    name = "",
    lastFour = "1233",
    metadata = mapOf(),
    message = "success msg",
    verificationUrl = "moyasar.com",
    createdAt = "",
    updatedAt = ""
  )

}