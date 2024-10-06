package com.moyasar.android.sdk.presentation.viewmodel

import android.app.Application
import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer
import com.moyasar.android.sdk.core.extensions.default
import com.moyasar.android.sdk.core.data.response.PaymentResponse
import com.moyasar.android.sdk.core.domain.entities.PaymentResult
import com.moyasar.android.sdk.creditcard.domain.usecases.CreatePaymentUseCase
import com.moyasar.android.sdk.creditcard.domain.usecases.CreateTokenUseCase
import com.moyasar.android.sdk.creditcard.presentation.model.PaymentConfig
import com.moyasar.android.sdk.creditcard.presentation.viewmodel.FormValidator
import com.moyasar.android.sdk.creditcard.presentation.viewmodel.PaymentSheetViewModel
import com.moyasar.android.sdk.presentation.viewmodel.TestDataHelper.createPaymentRequestBody
import com.moyasar.android.sdk.presentation.viewmodel.TestDataHelper.createTokenRequestBody
import com.moyasar.android.sdk.presentation.viewmodel.TestDataHelper.getPaymentBody
import com.moyasar.android.sdk.presentation.viewmodel.TestDataHelper.getTokenResponseBody
import com.moyasar.android.sdk.stcpay.domain.usecases.ValidateSTCPayOTPUseCase
import com.moyasar.android.sdk.stcpay.presentation.model.validation.STCPayFormValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.mock


/**
 * Created by Mahmoud Ashraf on 18,August,2024
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PaymentSheetViewModelTest {

  @Rule
  @JvmField
  val instantTaskExecutorRule = InstantTaskExecutorRule()

  @Test
  fun when_triggerCreatePayment_and_result_state_isSuccess() =
    runTest {
      // arrange
      val testScheduler = TestCoroutineScheduler()
      val testDispatcher = UnconfinedTestDispatcher(testScheduler)
      Dispatchers.setMain(testDispatcher)
      val createPaymentUseCase = mock(CreatePaymentUseCase::class.java)
      val validateSTCPayOTPUseCase = mock(ValidateSTCPayOTPUseCase::class.java)
      val createTokenUseCase = mock(CreateTokenUseCase::class.java)
      val formValidator = mock(FormValidator::class.java)
      val stcPayFormValidator = mock(STCPayFormValidator::class.java)
      val paymentConfig = mock(PaymentConfig::class.java)
      val app = mock(Application::class.java)
      val observer: Observer<PaymentResponse?> = mock()
      val captor = ArgumentCaptor.forClass(PaymentResponse::class.java)
      val request = createPaymentRequestBody()
      Mockito.`when`(createPaymentUseCase.invoke(request)).thenReturn(
        getPaymentBody().copy(status = "Success")
      )
      Mockito.`when`(formValidator._isFormValid).thenReturn(
        MediatorLiveData<Boolean>().default(false)
      )
      val viewModel = PaymentSheetViewModel(
        application = app,
        paymentConfig = paymentConfig,
        createPaymentUseCase = createPaymentUseCase,
        createTokenUseCase = createTokenUseCase,
        validateSTCPayOTPUseCase = validateSTCPayOTPUseCase,
        formValidator = formValidator,
        stcPayFormValidator = stcPayFormValidator,
        callback = {}
      )
      viewModel.payment.observeForever(observer)
      // act
      viewModel.createPayment(
        request = request,
        mainDispatcher = UnconfinedTestDispatcher(),
        ioDispatcher = UnconfinedTestDispatcher()
      )
      testScheduler.advanceUntilIdle()
      Mockito.verify(observer, Mockito.times(1))
        .onChanged(captor.capture())
      val values = captor.allValues
      val expectedResponse = getPaymentBody().copy(status = "Success")
      assertEquals(expectedResponse, values[0])
      viewModel.payment.removeObserver(observer)
      Dispatchers.resetMain()
    }

  @Test
  fun when_triggerCreateSaveOnlyToken_and_result_state_isSuccess_will_invoke_callback() =
    runTest {
      // arrange
      val testScheduler = TestCoroutineScheduler()
      val testDispatcher = UnconfinedTestDispatcher(testScheduler)
      Dispatchers.setMain(testDispatcher)
      val createPaymentUseCase = mock(CreatePaymentUseCase::class.java)
      val createTokenUseCase = mock(CreateTokenUseCase::class.java)
      val validateSTCPayOTPUseCase = mock(ValidateSTCPayOTPUseCase::class.java)
      val formValidator = mock(FormValidator::class.java)
      val stcPayFormValidator = mock(STCPayFormValidator::class.java)
      val paymentConfig = mock(PaymentConfig::class.java)
      val app = mock(Application::class.java)
      val callback: (PaymentResult) -> Unit = mock()
      val request = createTokenRequestBody()
      Mockito.`when`(createTokenUseCase.invoke(request)).thenReturn(
        getTokenResponseBody()
      )
      Mockito.`when`(formValidator._isFormValid).thenReturn(
        MediatorLiveData<Boolean>().default(false)
      )
      val viewModel = PaymentSheetViewModel(
        application = app,
        paymentConfig = paymentConfig,
        createPaymentUseCase = createPaymentUseCase,
        createTokenUseCase = createTokenUseCase,
        validateSTCPayOTPUseCase = validateSTCPayOTPUseCase,
        formValidator = formValidator,
        stcPayFormValidator = stcPayFormValidator,
        callback = callback
      )
      // act
      viewModel.createSaveOnlyToken(
        request = request,
        mainDispatcher = UnconfinedTestDispatcher(),
        ioDispatcher = UnconfinedTestDispatcher()
      )
      testScheduler.advanceUntilIdle()
      Mockito.verify(createTokenUseCase).invoke(request)
      Mockito.verify(callback).invoke(PaymentResult.CompletedToken(getTokenResponseBody()))
      Dispatchers.resetMain()
    }

  @Test
  fun when_triggerCreateSaveOnlyToken_and_result_state_isFail() =
    runTest {
      // arrange
      val testScheduler = TestCoroutineScheduler()
      val testDispatcher = UnconfinedTestDispatcher(testScheduler)
      Dispatchers.setMain(testDispatcher)
      val createPaymentUseCase = mock(CreatePaymentUseCase::class.java)
      val createTokenUseCase = mock(CreateTokenUseCase::class.java)
      val validateSTCPayOTPUseCase = mock(ValidateSTCPayOTPUseCase::class.java)
      val formValidator = mock(FormValidator::class.java)
      val stcPayFormValidator = mock(STCPayFormValidator::class.java)
      val paymentConfig = mock(PaymentConfig::class.java)
      val app = mock(Application::class.java)
      val callback: (PaymentResult) -> Unit = mock()
      val request = createTokenRequestBody()
      val exception = Exception("error")
      Mockito.`when`(createTokenUseCase.invoke(request)).then {
        throw exception
      }
      Mockito.`when`(formValidator._isFormValid).thenReturn(
        MediatorLiveData<Boolean>().default(false)
      )
      val viewModel = PaymentSheetViewModel(
        application = app,
        paymentConfig = paymentConfig,
        createPaymentUseCase = createPaymentUseCase,
        createTokenUseCase = createTokenUseCase,
        validateSTCPayOTPUseCase = validateSTCPayOTPUseCase,
        formValidator = formValidator,
        stcPayFormValidator = stcPayFormValidator,
        callback = callback
      )
      // act
      viewModel.createSaveOnlyToken(
        request = request,
        mainDispatcher = UnconfinedTestDispatcher(),
        ioDispatcher = UnconfinedTestDispatcher()
      )
      testScheduler.advanceUntilIdle()
      Mockito.verify(callback).invoke(PaymentResult.Failed(exception))
      Dispatchers.resetMain()
    }

  @Test
  fun when_triggerCreateSaveOnlyToken_and_result_state_isSuccess_will_invoke_UseCase() =
    runTest {
      // arrange
      val testScheduler = TestCoroutineScheduler()
      val testDispatcher = UnconfinedTestDispatcher(testScheduler)
      Dispatchers.setMain(testDispatcher)
      val createPaymentUseCase = mock(CreatePaymentUseCase::class.java)
      val createTokenUseCase = mock(CreateTokenUseCase::class.java)
      val validateSTCPayOTPUseCase = mock(ValidateSTCPayOTPUseCase::class.java)
      val formValidator = mock(FormValidator::class.java)
      val stcPayFormValidator = mock(STCPayFormValidator::class.java)
      val paymentConfig = mock(PaymentConfig::class.java)
      val app = mock(Application::class.java)
      val callback: (PaymentResult) -> Unit = mock()
      val request = createTokenRequestBody()
      Mockito.`when`(createTokenUseCase.invoke(request)).thenReturn(
        getTokenResponseBody()
      )
      Mockito.`when`(formValidator._isFormValid).thenReturn(
        MediatorLiveData<Boolean>().default(false)
      )
      val viewModel = PaymentSheetViewModel(
        application = app,
        paymentConfig = paymentConfig,
        createPaymentUseCase = createPaymentUseCase,
        createTokenUseCase = createTokenUseCase,
        validateSTCPayOTPUseCase = validateSTCPayOTPUseCase,
        formValidator = formValidator,
        stcPayFormValidator = stcPayFormValidator,
        callback = callback
      )
      // act
      viewModel.createSaveOnlyToken(
        request = request,
        mainDispatcher = UnconfinedTestDispatcher(),
        ioDispatcher = UnconfinedTestDispatcher()
      )
      testScheduler.advanceUntilIdle()
      Mockito.verify(createTokenUseCase).invoke(request)
      Dispatchers.resetMain()
    }

  @Test
  fun when_triggerCreatePayment_and_result_state_isFail() =
    runTest {
      // arrange
      val testScheduler = TestCoroutineScheduler()
      val testDispatcher = UnconfinedTestDispatcher(testScheduler)
      Dispatchers.setMain(testDispatcher)
      val createPaymentUseCase = mock(CreatePaymentUseCase::class.java)
      val createTokenUseCase = mock(CreateTokenUseCase::class.java)
      val validateSTCPayOTPUseCase = mock(ValidateSTCPayOTPUseCase::class.java)
      val formValidator = mock(FormValidator::class.java)
      val stcPayFormValidator = mock(STCPayFormValidator::class.java)
      val paymentConfig = mock(PaymentConfig::class.java)
      val app = mock(Application::class.java)
      val observer: Observer<PaymentResponse?> = mock()
      val captor = ArgumentCaptor.forClass(PaymentResponse::class.java)
      val request = createPaymentRequestBody()
      Mockito.`when`(createPaymentUseCase.invoke(request)).then {
        throw Exception("Error")
      }
      Mockito.`when`(formValidator._isFormValid).thenReturn(
        MediatorLiveData<Boolean>().default(false)
      )
      val viewModel = PaymentSheetViewModel(
        application = app,
        paymentConfig = paymentConfig,
        createPaymentUseCase = createPaymentUseCase,
        createTokenUseCase = createTokenUseCase,
        validateSTCPayOTPUseCase = validateSTCPayOTPUseCase,
        formValidator = formValidator,
        stcPayFormValidator = stcPayFormValidator,
        callback = {}
      )
      viewModel.payment.observeForever(observer)
      // act
      viewModel.createPayment(
        request = request,
        mainDispatcher = UnconfinedTestDispatcher(),
        ioDispatcher = UnconfinedTestDispatcher()
      )
      testScheduler.advanceUntilIdle()
      Mockito.verify(observer, Mockito.times(0))
        .onChanged(captor.capture())
      viewModel.payment.removeObserver(observer)
      Dispatchers.resetMain()
    }

  @Test
  fun when_triggerCreatePayment_and_result_state_isInitialed_then_return_PaymentAuth3dSecure() =
    runTest {
      // arrange
      val testScheduler = TestCoroutineScheduler()
      val testDispatcher = UnconfinedTestDispatcher(testScheduler)
      Dispatchers.setMain(testDispatcher)
      val createPaymentUseCase = mock(CreatePaymentUseCase::class.java)
      val createTokenUseCase = mock(CreateTokenUseCase::class.java)
      val validateSTCPayOTPUseCase = mock(ValidateSTCPayOTPUseCase::class.java)
      val formValidator = mock(FormValidator::class.java)
      val stcPayFormValidator = mock(STCPayFormValidator::class.java)
      val paymentConfig = mock(PaymentConfig::class.java)
      val app = mock(Application::class.java)
      val observer: Observer<PaymentResponse?> = mock()
      val captor = ArgumentCaptor.forClass(PaymentResponse::class.java)
      val request = createPaymentRequestBody()
      Mockito.`when`(createPaymentUseCase.invoke(request)).thenReturn(
        getPaymentBody().copy(status = "initiated")
      )
      Mockito.`when`(formValidator._isFormValid).thenReturn(
        MediatorLiveData<Boolean>().default(false)
      )
      val viewModel = PaymentSheetViewModel(
        application = app,
        paymentConfig = paymentConfig,
        createPaymentUseCase = createPaymentUseCase,
        createTokenUseCase = createTokenUseCase,
        validateSTCPayOTPUseCase = validateSTCPayOTPUseCase,
        formValidator = formValidator,
        stcPayFormValidator = stcPayFormValidator,
        callback = {}
      )
      viewModel.payment.observeForever(observer)
      // act
      viewModel.createPayment(
        request = request,
        mainDispatcher = UnconfinedTestDispatcher(),
        ioDispatcher = UnconfinedTestDispatcher()
      )
      Mockito.verify(observer, Mockito.times(1))
        .onChanged(captor.capture())
      val values = captor.allValues
      val expectedResponse = getPaymentBody().copy(status = "initiated")
      assertEquals(expectedResponse, values[0])
      viewModel.payment.removeObserver(observer)
      Dispatchers.resetMain()
    }

}


