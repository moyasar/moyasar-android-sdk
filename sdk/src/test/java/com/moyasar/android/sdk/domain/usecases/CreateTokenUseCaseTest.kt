package com.moyasar.android.sdk.domain.usecases

import com.moyasar.android.sdk.core.exceptions.ApiException
import com.moyasar.android.sdk.creditcard.domain.usecases.CreateTokenUseCase
import com.moyasar.android.sdk.core.data.response.ErrorResponse
import com.moyasar.android.sdk.creditcard.data.models.response.TokenResponse
import com.moyasar.android.sdk.creditcard.data.models.request.TokenRequest
import com.moyasar.android.sdk.creditcard.data.remote.PaymentService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import kotlin.test.assertFailsWith

/**
 * Created by Mahmoud Ashraf on 17,August,2024
 */
class CreateTokenUseCaseTest {

  @Mock
  private lateinit var paymentService: PaymentService
  private lateinit var createTokenUseCase: CreateTokenUseCase

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    createTokenUseCase = CreateTokenUseCase(paymentService)
  }

  @Test
  fun `invoke should call createToken and return the expected result`() = runTest {
    // Arrange
    val request = TokenRequest(
      name = "mahmoud ashraf", number = "1234123412341234", cvc = "123",
      month = "11", year = "2024", saveOnly = false, "moyasar.com", mapOf()
    )
    val expectedToken = TokenResponse(
      id = "1",
      status = "valid",
      brand = "123",
      funding = "12",
      country = "SA",
      month = "11",
      year = "2024",
      name = "Mahmoud Ashraf",
      lastFour = "1234",
      metadata = mapOf(),
      message = "success message",
      verificationUrl = "moyasar.url",
      createdAt = "17-8-2024",
      updatedAt = "17-8-2024"
    )
    Mockito.`when`(paymentService.createToken(request)).thenReturn(expectedToken)

    // Act
    val result = createTokenUseCase.invoke(request)

    // Assert
    assertEquals(expectedToken, result)
  }

  @Test
  fun `invoke should call createToken and invoke createToken in paymentService`() = runTest {
    // Arrange
    val request = TokenRequest(
      name = "mahmoud ashraf", number = "1234123412341234", cvc = "123",
      month = "11", year = "2024", saveOnly = false, "moyasar.com", mapOf()
    )
    val expectedToken = TokenResponse(
      id = "1",
      status = "valid",
      brand = "123",
      funding = "12",
      country = "SA",
      month = "11",
      year = "2024",
      name = "Mahmoud Ashraf",
      lastFour = "1234",
      metadata = mapOf(),
      message = "success message",
      verificationUrl = "moyasar.url",
      createdAt = "17-8-2024",
      updatedAt = "17-8-2024"
    )
    Mockito.`when`(paymentService.createToken(request)).thenReturn(expectedToken)

    // Act
    createTokenUseCase.invoke(request)

    // Assert
    Mockito.verify(paymentService).createToken(request)
  }

  @Test
  fun `invoke should throw an exception when paymentService createToken fails`() = runTest {
    // Arrange
    val request = TokenRequest(
      name = "mahmoud ashraf", number = "1234123412341234", cvc = "123",
      month = "11", year = "2024", saveOnly = false, "moyasar.com", mapOf()
    )
    val errorResponse =
      ErrorResponse(message = "Failed to create token", type = "server_error", errors = null)
    val exception = ApiException(errorResponse)
    Mockito.`when`(paymentService.createToken(request)).then {
      throw exception
    }
    // Act & Assert
    val thrown = assertFailsWith<ApiException> {
      createTokenUseCase.invoke(request)
    }
    // Verify the exception message
    assertEquals("Failed to create token", thrown.response.message)
  }

}