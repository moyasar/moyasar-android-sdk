package com.moyasar.android.sdkdriver

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.moyasar.android.sdk.core.customviews.button.MoyasarButtonType
import com.moyasar.android.sdk.core.domain.entities.PaymentResult
import com.moyasar.android.sdk.core.extensions.default
import com.moyasar.android.sdk.core.data.response.PaymentResponse
import com.moyasar.android.sdk.creditcard.data.models.CreditCardNetwork
import com.moyasar.android.sdk.creditcard.data.models.request.PaymentRequest
import com.moyasar.android.sdk.stcpay.presentation.view.fragments.EnterMobileNumberFragment
import com.moyasar.android.sdk.creditcard.presentation.view.fragments.PaymentFragment
import com.moyasar.android.sdk.samsungpay.data.SamsungPayConfig
import com.moyasar.android.sdk.samsungpay.presentation.SamsungPayFragment
import com.moyasar.android.sdkdriver.customui.creditcard.CustomUIPaymentFragment
import com.moyasar.android.sdkdriver.customui.stcpay.EnterMobileNumberCustomUIFragment
import kotlinx.parcelize.Parcelize
import java.util.UUID

class CheckoutViewModel : ViewModel() {
    val status = MutableLiveData<Status>().default(Status.Idle)
    private val payment = MutableLiveData<PaymentResponse?>()
    private val paymentRequest = PaymentRequest(
        apiKey = "pk_test_U38gMHTgVv4wYCd35Zk1JSEd1ZyMYyA9oQ7T4rKa",
        amount = 20001,
        currency = "SAR",
        givenID = UUID.randomUUID().toString(), // generate from your side uuid (v4 is recommended) to apply Idempotency or keep it null
        description = "Test payment",
        metadata = mapOf(
            "order_id" to "order_123"
        ),
        manual = false,
        baseUrl = "https://api.moyasar.com",
        buttonType = MoyasarButtonType.PAY,
        allowedNetworks = listOf(CreditCardNetwork.Mastercard, CreditCardNetwork.Visa, CreditCardNetwork.Amex, CreditCardNetwork.Mada),
        createSaveOnlyToken = false,
        applyCoupon = true,
        merchantCountryCode = "SA",
        // optional object for Samsung-pay
        samsungPay = SamsungPayConfig(
            serviceId = "ea810dafb758408fa530b1",
            merchantName = "Test Samsung Pay from app",
            orderNumber = "c553ed70-fb79-487c-b3d2-15aca6aff90c",
            manual = false
        )
    )

    // For demo purposes only
    @SuppressLint("StaticFieldLeak")
    private lateinit var activity: CheckoutActivity
    private lateinit var paymentFragment: PaymentFragment
    private lateinit var customUIPaymentFragment: CustomUIPaymentFragment
    private lateinit var enterMobileNumberFragment: EnterMobileNumberFragment
    private lateinit var enterMobileNumberCustomUIFragment: EnterMobileNumberCustomUIFragment
    private lateinit var samsungPayFragment: SamsungPayFragment

    private fun onCreditCardPaymentResult(result: PaymentResult) {
        activity.runOnUiThread {
            activity.supportFragmentManager.beginTransaction().remove(paymentFragment).commit()
        }

        handlePaymentResult(result)
    }
    private fun onCustomUICreditCardPaymentResult(result: PaymentResult) {
        activity.runOnUiThread {
            activity.supportFragmentManager.beginTransaction().remove(customUIPaymentFragment).commit()
        }

        handlePaymentResult(result)
    }

    private fun onSTCPayPaymentResult(result: PaymentResult) {
        activity.runOnUiThread {
            activity.supportFragmentManager.beginTransaction().remove(enterMobileNumberFragment).commit()
        }

        handlePaymentResult(result)
    }

 private fun onCustomUISTCPayPaymentResult(result: PaymentResult) {
        activity.runOnUiThread {
            activity.supportFragmentManager.beginTransaction().remove(enterMobileNumberCustomUIFragment).commit()
        }

        handlePaymentResult(result)
    }

    private fun onSamsungPayPaymentResult(result: PaymentResult) {
        activity.runOnUiThread {
            activity.supportFragmentManager.beginTransaction().remove(samsungPayFragment).commit()
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

        this.paymentFragment = PaymentFragment.newInstance(activity.application, paymentRequest) { this.onCreditCardPaymentResult(it) }

        activity.supportFragmentManager.beginTransaction().apply {
            replace(id, paymentFragment)
            commit()
        }
    }

    fun beginDonationWithCreditCardCustomUI(activity: CheckoutActivity, id: Int) {
        this.activity = activity

        this.customUIPaymentFragment = CustomUIPaymentFragment.newInstance(activity.application, paymentRequest) { this.onCustomUICreditCardPaymentResult(it) }

        activity.supportFragmentManager.beginTransaction().apply {
            replace(id, customUIPaymentFragment)
            commit()
        }
    }

    fun beginDonationWithSTC(activity: CheckoutActivity, id: Int) {
        this.activity = activity

        this.enterMobileNumberFragment = EnterMobileNumberFragment.newInstance(activity.application, paymentRequest) { this.onSTCPayPaymentResult(it) }

        activity.supportFragmentManager.beginTransaction().apply {
            replace(id, enterMobileNumberFragment)
            commit()
        }
    }

    fun beginDonationWithSTCCustomUI(activity: CheckoutActivity, id: Int) {
        this.activity = activity

        this.enterMobileNumberCustomUIFragment = EnterMobileNumberCustomUIFragment.newInstance(activity.application, paymentRequest) { this.onCustomUISTCPayPaymentResult(it) }

        activity.supportFragmentManager.beginTransaction().apply {
            replace(id, enterMobileNumberCustomUIFragment)
            commit()
        }
    }

    fun beginDonationWithSamsungPay(activity: CheckoutActivity, id: Int) {
        this.activity = activity
        this.samsungPayFragment = SamsungPayFragment.newInstance(
            activity.application,
            paymentRequest
        ) { this.onSamsungPayPaymentResult(it) }

        activity.supportFragmentManager.beginTransaction().apply {
            replace(id, samsungPayFragment)
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
