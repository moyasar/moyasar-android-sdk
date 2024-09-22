package com.moyasar.android.sdk.creditcard.presentation.di

import android.app.Application
import com.moyasar.android.sdk.creditcard.data.remote.PaymentService
import com.moyasar.android.sdk.stcpay.data.remote.STCPayPaymentService
import com.moyasar.android.sdk.core.domain.entities.PaymentResult
import com.moyasar.android.sdk.creditcard.domain.usecases.CreatePaymentUseCase
import com.moyasar.android.sdk.creditcard.domain.usecases.CreateTokenUseCase
import com.moyasar.android.sdk.stcpay.domain.usecases.ValidateSTCPayOTPUseCase
import com.moyasar.android.sdk.creditcard.presentation.model.PaymentConfig
import com.moyasar.android.sdk.creditcard.presentation.viewmodel.FormValidator
import com.moyasar.android.sdk.creditcard.presentation.viewmodel.PaymentSheetViewModel

/**
 * Created by Mahmoud Ashraf on 26,July,2024
 */
object MoyasarAppContainer {

  private lateinit var application: Application
  private lateinit var config: PaymentConfig
  private lateinit var callback: (PaymentResult) -> Unit

  private val paymentService: PaymentService by lazy {
    PaymentService(
      config.apiKey,
      config.baseUrl
    )
  }

  private val stcPayPaymentService: STCPayPaymentService by lazy {
    STCPayPaymentService()
  }

  private val createPaymentUseCase by lazy {
    CreatePaymentUseCase(paymentService)
  }

  private val validateSTCPayOTPUseCase by lazy {
    ValidateSTCPayOTPUseCase(stcPayPaymentService)
  }

  private val createTokenUseCase by lazy {
    CreateTokenUseCase(paymentService)
  }

  private val formValidator by lazy {
    FormValidator(application)
  }

  internal val viewModel by lazy {
    PaymentSheetViewModel(
      application = application,
      paymentConfig = config,
      callback = callback,
      formValidator = formValidator,
      createPaymentUseCase = createPaymentUseCase,
      createTokenUseCase = createTokenUseCase,
      validateSTCPayOTPUseCase = validateSTCPayOTPUseCase
    )
  }

  fun initialize(
    application: Application,
    config: PaymentConfig,
    callback: (PaymentResult) -> Unit,
  ) {
    MoyasarAppContainer.application = application
    MoyasarAppContainer.config = config
    MoyasarAppContainer.callback = callback
  }
}
