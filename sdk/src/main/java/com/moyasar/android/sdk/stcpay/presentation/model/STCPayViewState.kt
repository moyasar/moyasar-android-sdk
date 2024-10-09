package com.moyasar.android.sdk.stcpay.presentation.model

import android.os.Parcelable
import com.moyasar.android.sdk.creditcard.presentation.model.PaymentStatusViewState
import kotlinx.parcelize.Parcelize

/**
 * Created by Mahmoud Ashraf on 02,October,2024
 */
sealed class STCPayViewState : Parcelable {
    @Parcelize
    data object Init : STCPayViewState()

    @Parcelize
    data object SubmittingSTCPayOTP : STCPayViewState()

    @Parcelize
    data class STCPayOTPAuth(val url: String) : STCPayViewState()

    @Parcelize
    data object SubmittingSTCPayMobileNumber : STCPayViewState()
}