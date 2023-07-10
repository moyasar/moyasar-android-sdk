package com.moyasar.sdkdriver

import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.moyasar.android.sdk.PaymentConfig
import com.moyasar.android.sdk.PaymentResult
import com.moyasar.android.sdk.PaymentSheet
import com.moyasar.android.sdk.StcPaySheet
import com.moyasar.android.sdk.payment.models.Payment
import kotlinx.parcelize.Parcelize

class CheckoutViewModel : ViewModel() {
    val status = MutableLiveData<Status>(Status.Idle)
    val payment = MutableLiveData<Payment?>(null)
    var paymentSheet: PaymentSheet? = null
    var stcPaySheet: StcPaySheet? = null
    val config = PaymentConfig(
        amount = 100,
        currency = "SAR",
        description = "Sample Android SDK Payment",
//        apiKey = "pk_live_TH6rVePGHRwuJaAtoJ1LsRfeKYovZgC1uddh7NdX",
        apiKey = "pk_test_vcFUHJDBwiyRu4Bd3hFuPpTnRPY4gp2ssYdNJMY3",
        baseUrl = "https://api.moyasar.com",
        metadata = mapOf(
            "order_id" to "order_123"
        )
    )
    val stcpayConfig = PaymentConfig(
        amount = 100,
        currency = "SAR",
        description = "Sample Android SDK Payment",
//        apiKey = "pk_live_TH6rVePGHRwuJaAtoJ1LsRfeKYovZgC1uddh7NdX",
        apiKey = "pk_live_g2Cc9uHPz5hk1GVGbXegK3dC8wyG8n2QQtdWECn2",
        baseUrl = "https://apimig.moyasar.com",
        metadata = mapOf(
            "order_id" to "order_123"
        )
    )

    fun registerForActivity(activity: CheckoutActivity) {
//        paymentSheet = PaymentSheet(activity, { this.onPaymentSheetResult(it) }, config)
        stcPaySheet = StcPaySheet(activity, { this.onPaymentSheetResult(it) }, stcpayConfig)
    }

    fun beginDonation() {
        when (paymentSheet == null && stcPaySheet == null) {

            true -> {

                if (paymentSheet == null) {
                    status.value =
                        Status.Failed(Exception("Payment sheet was not setup for view model"))
                    return
                } else if (stcPaySheet == null) {
                    status.value =
                        Status.Failed(Exception("Payment sheet was not setup for view model"))
                    return
                }
            }

            false -> {
                stcPaySheet!!.present()
                Log.d("CheckoutViewModel().Status: ",status.value.toString())
//                paymentSheet!!.present()
            }
        }

    }

    fun onPaymentSheetResult(result: PaymentResult) {
        when (result) {
            PaymentResult.Canceled -> status.value = Status.Failed(Exception("User canceled"))
            is PaymentResult.Failed -> status.value = Status.Failed(Exception("Payment failed"))
            is PaymentResult.Completed -> {
                payment.value = result.payment
                when (result.payment.status) {
                    "paid", "authorized" -> status.value = Status.Success
                    else -> status.value = Status.Success
                    //Failed(Exception("Invalid OTP value"))
                }
            }
        }
        Log.d("onPaymentSheetResult(): Status: ",status.value.toString())
    }

    sealed class Status : Parcelable {
        @Parcelize
        object Idle : Status()

        @Parcelize
        object Success : Status()

        @Parcelize
        data class Failed(val e: Exception) : Status()
    }
}
