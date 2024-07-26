package com.moyasar.android.sdk.domain.usecases

import com.moyasar.android.sdk.data.models.TokenRequest
import com.moyasar.android.sdk.data.remote.PaymentService

class CreateTokenUseCase(private val paymentService: PaymentService) {
  suspend operator fun invoke(request: TokenRequest) = paymentService.createToken(request)
}