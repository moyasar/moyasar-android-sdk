package com.moyasar.android.sdk.core.util

import com.moyasar.android.sdk.creditcard.data.models.request.PaymentRequest
import com.moyasar.android.sdk.creditcard.data.models.request.TokenRequest
import com.moyasar.android.sdk.creditcard.data.models.sources.CardPaymentSource
import com.moyasar.android.sdk.stcpay.data.models.request.STCPayOTPRequest

private const val MASK = "****"

private fun maskCardNumber(number: String): String {
    if (number.length <= 4) return MASK
    return MASK + number.takeLast(4)
}

/**
 * Redacts the PAN/CVC before the request is serialized for logging, since raw
 * card data must never be written to Logcat (PCI-DSS).
 */
fun TokenRequest.redactedForLogging(): TokenRequest = copy(
    number = maskCardNumber(number),
    cvc = cvc?.let { MASK }
)

fun PaymentRequest.redactedForLogging(): PaymentRequest {
    val source = source
    return if (source is CardPaymentSource) {
        copy(
            source = source.copy(
                number = maskCardNumber(source.number),
                cvc = MASK
            )
        )
    } else {
        this
    }
}

fun STCPayOTPRequest.redactedForLogging(): STCPayOTPRequest = copy(otp = MASK)
