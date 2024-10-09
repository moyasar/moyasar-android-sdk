package com.moyasar.android.sdk.creditcard.domain.usecases

import com.moyasar.android.sdk.creditcard.data.models.request.PaymentRequest
import com.moyasar.android.sdk.creditcard.data.remote.PaymentService

class CreatePaymentUseCase(private val paymentService: PaymentService) {
  suspend operator fun invoke(request: PaymentRequest) = paymentService.create(request)
}