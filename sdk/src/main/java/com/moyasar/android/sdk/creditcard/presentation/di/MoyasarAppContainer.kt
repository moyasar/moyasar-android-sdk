package com.moyasar.android.sdk.creditcard.presentation.di

import android.app.Application
import com.moyasar.android.sdk.creditcard.data.remote.PaymentService
import com.moyasar.android.sdk.stcpay.data.remote.STCPayPaymentService
import com.moyasar.android.sdk.core.domain.entities.PaymentResult
import com.moyasar.android.sdk.creditcard.data.models.request.PaymentRequest
import com.moyasar.android.sdk.creditcard.domain.usecases.CreatePaymentUseCase
import com.moyasar.android.sdk.creditcard.domain.usecases.CreateTokenUseCase
import com.moyasar.android.sdk.stcpay.domain.usecases.ValidateSTCPayOTPUseCase
import com.moyasar.android.sdk.creditcard.presentation.viewmodel.PaymentSheetViewModel

/**
 * Created by Mahmoud Ashraf on 26,July,2024
 */
object MoyasarAppContainer {

  internal lateinit var application: Application
  internal lateinit var paymentRequest: PaymentRequest
  private lateinit var callback: (PaymentResult) -> Unit

  private val paymentService: PaymentService by lazy {
    PaymentService(
      paymentRequest.apiKey,
      paymentRequest.baseUrl
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

val allowedNetworks
  get() = paymentRequest.allowedNetworks

  private var _viewModel : PaymentSheetViewModel? = null

   val viewModel: PaymentSheetViewModel
    get() {
      return synchronized(this) {
        if (_viewModel == null) {
          _viewModel = PaymentSheetViewModel(
            application = application,
            paymentRequest = paymentRequest,
            callback = callback,
            createPaymentUseCase = createPaymentUseCase,
            createTokenUseCase = createTokenUseCase,
            validateSTCPayOTPUseCase = validateSTCPayOTPUseCase
          )
        }
        _viewModel!!
      }
    }
  fun initialize(
    application: Application,
    paymentRequest: PaymentRequest,
    callback: (PaymentResult) -> Unit,
  ) {
    release()
    MoyasarAppContainer.application = application
    MoyasarAppContainer.paymentRequest = paymentRequest
    MoyasarAppContainer.callback = callback
  }

  private fun release() {
    _viewModel= null
  }
}
