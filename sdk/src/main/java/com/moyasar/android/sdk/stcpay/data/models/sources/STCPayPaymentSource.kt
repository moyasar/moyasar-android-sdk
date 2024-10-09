package com.moyasar.android.sdk.stcpay.data.models.sources

import com.moyasar.android.sdk.core.data.PaymentSource

/**
 * Created by Mahmoud Ashraf on 22,September,2024
 */
data class STCPayPaymentSource(val mobile: String, val type: String = "stcpay") : PaymentSource