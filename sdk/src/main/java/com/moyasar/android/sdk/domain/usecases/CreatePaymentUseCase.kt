package com.moyasar.android.sdk.domain.usecases

import com.moyasar.android.sdk.data.models.PaymentRequest
import com.moyasar.android.sdk.data.remote.PaymentService

class CreatePaymentUseCase(private val paymentService: PaymentService) {
  suspend operator fun invoke(request: PaymentRequest) = paymentService.create(request)
}