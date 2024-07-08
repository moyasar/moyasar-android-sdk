package com.moyasar.android.sdk.domain.usecases

import com.moyasar.android.sdk.data.models.TokenRequest
import com.moyasar.android.sdk.data.remote.PaymentService

/**
 * Created by Mahmoud Ashraf on 08,July,2024
 */
class CreateTokenUseCase(private val apiKey: String, private val baseUrl: String) {
  private val _paymentService: PaymentService by lazy { PaymentService(apiKey, baseUrl) }

  suspend operator fun invoke(request: TokenRequest) = _paymentService.createToken(request)
}