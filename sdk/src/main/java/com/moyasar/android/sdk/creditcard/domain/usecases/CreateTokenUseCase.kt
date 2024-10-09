package com.moyasar.android.sdk.creditcard.domain.usecases

import com.moyasar.android.sdk.creditcard.data.models.request.TokenRequest
import com.moyasar.android.sdk.creditcard.data.remote.PaymentService

class CreateTokenUseCase(private val paymentService: PaymentService) {
  suspend operator fun invoke(request: TokenRequest) = paymentService.createToken(request)
}