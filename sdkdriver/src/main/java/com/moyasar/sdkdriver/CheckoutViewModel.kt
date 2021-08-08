package com.moyasar.sdkdriver

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moyasar.android.sdk.PaymentConfig
import com.moyasar.android.sdk.PaymentResult
import com.moyasar.android.sdk.PaymentSheet
import com.moyasar.android.sdk.payment.Payment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CheckoutViewModel : ViewModel() {
    val status = MutableLiveData(Status.Idle)
    val payment = MutableLiveData<Payment?>(null)

    fun beginDonation(context: CheckoutActivity) {
        val config = PaymentConfig(
            amount = 100,
            currency = "SAR",
            description = "Sample Android SDK Payment",
            apiKey = "pk_test_vcFUHJDBwiyRu4Bd3hFuPpTnRPY4gp2ssYdNJMY3"
        )

        PaymentSheet(context, {
            viewModelScope.launch (Dispatchers.Default) {
                when (it) {
                    PaymentResult.Canceled -> status.value = Status.Failed
                    is PaymentResult.Failed -> status.value = Status.Failed
                    is PaymentResult.Completed -> {
                        payment.value = it.payment
                        when (it.payment.status) {
                            "paid", "authorized" -> status.value = Status.Success
                            else -> status.value = Status.Success
                        }
                    }
                }
            }
        }, config).present()
    }

    enum class Status {
        Idle,
        Success,
        Failed
    }
}
