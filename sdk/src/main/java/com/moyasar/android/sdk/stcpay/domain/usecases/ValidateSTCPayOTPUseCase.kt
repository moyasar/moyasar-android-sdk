package com.moyasar.android.sdk.stcpay.domain.usecases

import com.moyasar.android.sdk.stcpay.data.remote.STCPayPaymentService

class ValidateSTCPayOTPUseCase(private val stcPayPaymentService: STCPayPaymentService) {
  suspend operator fun invoke(transactionURL: String,otp: String) = stcPayPaymentService.validateSTCPayOTP(transactionURL, otp)
}