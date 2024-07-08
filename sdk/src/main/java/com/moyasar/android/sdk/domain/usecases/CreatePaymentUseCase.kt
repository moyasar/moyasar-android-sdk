package com.moyasar.android.sdk.domain.usecases

import com.moyasar.android.sdk.data.models.PaymentRequest
import com.moyasar.android.sdk.data.remote.PaymentService

/**
 * Created by Mahmoud Ashraf on 08,July,2024
 */
class CreatePaymentUseCase(private val apiKey: String, private val baseUrl: String) {
  private val _paymentService: PaymentService by lazy { PaymentService(apiKey, baseUrl) }

  suspend operator fun invoke(request: PaymentRequest) = _paymentService.create(request)
}