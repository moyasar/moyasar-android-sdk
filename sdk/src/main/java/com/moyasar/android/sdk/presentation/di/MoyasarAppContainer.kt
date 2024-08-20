package com.moyasar.android.sdk.presentation.di

import android.app.Application
import com.moyasar.android.sdk.data.remote.PaymentService
import com.moyasar.android.sdk.domain.entities.PaymentResult
import com.moyasar.android.sdk.domain.usecases.CreatePaymentUseCase
import com.moyasar.android.sdk.domain.usecases.CreateTokenUseCase
import com.moyasar.android.sdk.presentation.model.PaymentConfig
import com.moyasar.android.sdk.presentation.viewmodel.FormValidator
import com.moyasar.android.sdk.presentation.viewmodel.PaymentSheetViewModel

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

  private val createPaymentUseCase by lazy {
    CreatePaymentUseCase(paymentService)
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
      createTokenUseCase = createTokenUseCase
    )
  }

  fun initialize(
    application: Application,
    config: PaymentConfig,
    callback: (PaymentResult) -> Unit,
  ) {
    this.application = application
    this.config = config
    this.callback = callback
  }
}
