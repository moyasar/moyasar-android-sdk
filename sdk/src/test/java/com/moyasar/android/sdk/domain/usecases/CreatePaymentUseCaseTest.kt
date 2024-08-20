package com.moyasar.android.sdk.domain.usecases

import com.moyasar.android.sdk.core.exceptions.ApiException
import com.moyasar.android.sdk.data.models.CardPaymentSource
import com.moyasar.android.sdk.data.models.ErrorResponse
import com.moyasar.android.sdk.data.models.Payment
import com.moyasar.android.sdk.data.models.PaymentRequest
import com.moyasar.android.sdk.data.remote.PaymentService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.junit.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import kotlin.test.assertFailsWith

class CreatePaymentUseCaseTest {
  @Mock
  private lateinit var paymentService: PaymentService
  private lateinit var createPaymentUseCase: CreatePaymentUseCase

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    createPaymentUseCase = CreatePaymentUseCase(paymentService)
  }

  @Test
  fun `invoke should return result when paymentService create is successful`() = runTest {
    // Arrange
    val request = PaymentRequest(
      amount = 1000,
      currency = "SAR",
      description = "",
      callbackUrl = "",
      source = CardPaymentSource(
        name = "asd", number = "1234123412341234", month = "11", year = "2024", cvc = "123",
        manual = null, saveCard = null
      ),
    )
    val expectedResult = Payment(
      "1",
      "success",
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
      "", "",
      "", "",
      mapOf(),
      mutableMapOf()
    )
    Mockito.`when`(paymentService.create(request)).thenReturn(expectedResult)

    // Act
    val result = createPaymentUseCase(request)

    // Assert
    assertEquals(expectedResult, result)
  }

  @Test
  fun `invoke should invoke paymentService create() function`() = runTest {
    // Arrange
    val request = PaymentRequest(
      amount = 1000,
      currency = "SAR",
      description = "",
      callbackUrl = "",
      source = CardPaymentSource(
        name = "asd", number = "1234123412341234", month = "11", year = "2024", cvc = "123",
        manual = null, saveCard = null
      ),
    )
    val expectedResult = Payment(
      "1",
      "success",
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
      "", "",
      "", "",
      mapOf(),
      mutableMapOf()
    )
    Mockito.`when`(paymentService.create(request)).thenReturn(expectedResult)

    // Act
    createPaymentUseCase(request)

    // Assert
    Mockito.verify(paymentService).create(request)
  }

  @Test
  fun `invoke should throw an exception when paymentService create fails`() = runTest {
    // Arrange
    val request = PaymentRequest(
      amount = 1000,
      currency = "SAR",
      description = "",
      callbackUrl = "",
      source = CardPaymentSource(
        name = "asd", number = "1234123412341234", month = "11", year = "2024", cvc = "123",
        manual = null, saveCard = null
      ),
    )
    val errorResponse =
      ErrorResponse(message = "Payment error", type = "server_error", errors = null)
    val exception = ApiException(errorResponse)
    Mockito.`when`(paymentService.create(request)).then {
      throw exception
    }

    // Act & Assert
    val thrown = assertFailsWith<ApiException> {
      createPaymentUseCase(request)
    }
    // Verify the exception message
    assertEquals("Payment error", thrown.response.message)
  }
}