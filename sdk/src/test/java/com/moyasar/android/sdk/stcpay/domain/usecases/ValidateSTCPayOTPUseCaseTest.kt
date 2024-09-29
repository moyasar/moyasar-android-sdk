package com.moyasar.android.sdk.stcpay.domain.usecases

import com.moyasar.android.sdk.core.data.response.PaymentResponse
import com.moyasar.android.sdk.stcpay.data.remote.STCPayPaymentService
import org.junit.Assert.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations


/**
 * Created by Mahmoud Ashraf on 29,September,2024
 */
@RunWith(MockitoJUnitRunner::class)
class ValidateSTCPayOTPUseCaseTest {

    @Mock
    private lateinit var stcPayPaymentService: STCPayPaymentService

    private lateinit var validateSTCPayOTPUseCase: ValidateSTCPayOTPUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        validateSTCPayOTPUseCase = ValidateSTCPayOTPUseCase(stcPayPaymentService)
    }

    @Test
    fun `invoke should call validateSTCPayOTP and return result`() = runTest {
        // Arrange
        val transactionURL = "https://transaction.url"
        val otp = "1234"
        val expectedResult = PaymentResponse(
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

        // Mock the behavior of the service
        `when`(stcPayPaymentService.validateSTCPayOTP(transactionURL, otp)).thenReturn(expectedResult)

        // Act
        val result = validateSTCPayOTPUseCase(transactionURL, otp)

        // Assert
        assertEquals(expectedResult, result)
        verify(stcPayPaymentService).validateSTCPayOTP(transactionURL, otp)
    }
}
