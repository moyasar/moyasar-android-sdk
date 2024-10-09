package com.moyasar.android.sdk.stcpay.data.models.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Mahmoud Ashraf on 22,September,2024
 */
data class STCPayOTPRequest(@SerializedName("otp_value") val otp: String)
