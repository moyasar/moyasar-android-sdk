package com.moyasar.android.sdkdriver

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.Parcelable
import com.moyasar.android.sdk.creditcard.presentation.model.PaymentConfig
import com.moyasar.android.sdk.core.domain.entities.PaymentResult
import com.moyasar.android.sdk.core.extensions.default
import com.moyasar.android.sdk.core.data.response.PaymentResponse
import com.moyasar.android.sdk.creditcard.data.models.CreditCardNetwork
import com.moyasar.android.sdk.stcpay.presentation.view.fragments.EnterMobileNumberFragment
import com.moyasar.android.sdk.creditcard.presentation.view.fragments.PaymentFragment
import kotlinx.parcelize.Parcelize

class CheckoutViewModel : ViewModel() {
    val status = MutableLiveData<Status>().default(Status.Idle)
    private val payment = MutableLiveData<PaymentResponse?>()
    private val config = PaymentConfig(
        amount = 100000,
        currency = "SAR",
        description = "Sample Android SDK Payment",
        apiKey = "pk_test_vcFUHJDBwiyRu4Bd3hFuPpTnRPY4gp2ssYdNJMY3",
        baseUrl = "https://api.moyasar.com",
        manual = false,
        metadata = mapOf(
            "order_id" to "order_123"
        ),
        allowedNetworks = listOf(CreditCardNetwork.Mastercard),
        createSaveOnlyToken = false
    )

    // For demo purposes only
    private lateinit var activity: CheckoutActivity
    private lateinit var paymentFragment: PaymentFragment
    private lateinit var enterMobileNumberFragment: EnterMobileNumberFragment

    private fun onCreditCardPaymentResult(result: PaymentResult) {
        activity.runOnUiThread {
            activity.supportFragmentManager.beginTransaction().remove(paymentFragment).commit()
        }

        handlePaymentResult(result)
    }

    private fun onSTCPayPaymentResult(result: PaymentResult) {
        activity.runOnUiThread {
            activity.supportFragmentManager.beginTransaction().remove(enterMobileNumberFragment).commit()
        }

        handlePaymentResult(result)
    }

    private fun handlePaymentResult(result: PaymentResult) {
        when (result) {
            PaymentResult.Canceled -> {
                status.value = Status.Failed(Exception("User canceled"))
                println("User canceled")
            }

            is PaymentResult.Failed -> {
                status.value = Status.Failed(Exception("Payment failed"))
                println("Payment failed: ${result.error}")
            }

            is PaymentResult.Completed -> {
                payment.value = result.payment
                println("Payment: ${result.payment}")

                when (result.payment.status) {
                    "paid", "authorized" -> status.value = Status.Success
                    else -> {
                        status.value =
                            Status.Failed(Exception("Payment status isn't paid or authorized"))
                        println("Payment status isn't 'paid' or 'authorized', it's: '${result.payment.status}'")
                    }
                }
            }

            is PaymentResult.CompletedToken -> {
                println("Got newly created token: '${result.token.id}'")
            }
        }
    }

    fun beginDonationWithCreditCard(activity: CheckoutActivity, id: Int) {
        this.activity = activity

        this.paymentFragment = PaymentFragment.newInstance(activity.application, config) { this.onCreditCardPaymentResult(it) }

        activity.supportFragmentManager.beginTransaction().apply {
            replace(id, paymentFragment)
            commit()
        }
    }

    fun beginDonationWithSTC(activity: CheckoutActivity, id: Int) {
        this.activity = activity

        this.enterMobileNumberFragment = EnterMobileNumberFragment.newInstance(activity.application, config) { this.onSTCPayPaymentResult(it) }

        activity.supportFragmentManager.beginTransaction().apply {
            replace(id, enterMobileNumberFragment)
            commit()
        }
    }

    sealed class Status : Parcelable {
        @Parcelize
        data object Idle : Status()

        @Parcelize
        data object Success : Status()

        @Parcelize
        data class Failed(val e: Exception) : Status()
    }
}
