package com.moyasar.android.sdk.payment

import com.moyasar.android.sdk.payment.models.PaymentRequest
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PaymentService {
    @POST("v1/payments")
    suspend fun create(@Body request: PaymentRequest): Response<Payment>
}
