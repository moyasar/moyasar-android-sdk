package com.moyasar.android.sdk.data

import android.content.res.Resources
import android.os.Parcelable
import android.text.Editable
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import com.moyasar.android.sdk.PaymentConfig
import com.moyasar.android.sdk.PaymentResult
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.exceptions.ApiException
import com.moyasar.android.sdk.exceptions.PaymentSheetException
import com.moyasar.android.sdk.payment.PaymentService
import com.moyasar.android.sdk.payment.models.Payment
import com.moyasar.android.sdk.payment.models.PaymentRequest
import com.moyasar.android.sdk.payment.models.StcPaymentSource
import com.moyasar.android.sdk.ui.OtpAuthActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import java.text.NumberFormat
import java.util.Currency
import kotlin.collections.set

class StcPaySheetViewModel(
    private val paymentConfig: PaymentConfig, private val resources: Resources
) : ViewModel() {

    val _paymentService: PaymentService by lazy {
        PaymentService(paymentConfig.apiKey, paymentConfig.baseUrl)
    }

    var ccOnChangeLocked = false

    val status = MutableLiveData<Status>(Status.Reset)
    val payment = MutableLiveData<Payment?>(null)
    val _isFormValid = MediatorLiveData<Boolean>()
    val _sheetResult = MutableLiveData<PaymentResult?>(null)

    internal val sheetResult: LiveData<PaymentResult?>
        get() = _sheetResult.distinctUntilChanged()

    val number = MutableLiveData("")
    val otp = MutableLiveData<String>()
    val message = MutableLiveData<String>()

    val numberValidator = LiveDataValidator(number).apply {
        addRule("Phone number is required") { it.isNullOrBlank() }
        addRule("Phone number must start with (05)") { !cleanPhoneNumber.startsWith("05") }
        addRule("Phone number length must be 10 digits") { cleanPhoneNumber.length != 10 }
    }

    fun validateForm(): Boolean {
        return numberValidator.isValid().also { _isFormValid.value = it }
    }

    val otpValue: String
        get() = otp.value.toString()

    val cleanPhoneNumber: String
        get() = number.value!!.replace(" ", "")


    val payLabel: String
        get() {
            val currency = Currency.getInstance(paymentConfig.currency)
            val formatter = NumberFormat.getCurrencyInstance()
            formatter.currency = currency
            formatter.minimumFractionDigits = currency.defaultFractionDigits

            val label = resources.getString(R.string.payBtnLabel)

            val amount = formatter.format(
                100 / (Math.pow(
                    10.0, formatter.currency!!.defaultFractionDigits.toDouble()
                ))
            )
            when (status.value) {
                is Status.PaymentOtpSecure -> return "Verify OTP"
                else -> {}
            }

            return "$label $amount"
        }

    val amountLabel: String
        get() {
            val currency = Currency.getInstance(paymentConfig.currency)
            val formatter = NumberFormat.getCurrencyInstance()
            formatter.currency = currency
            formatter.minimumFractionDigits = currency.defaultFractionDigits

            return formatter.format(
                100 / (Math.pow(
                    10.0, formatter.currency!!.defaultFractionDigits.toDouble()
                ))
            )
        }

    fun submit() {
        if (!validateForm()) {
            return;
        }

        if (status.value != Status.Reset) {
            return
        }

        status.value = Status.SubmittingPayment

        val request = PaymentRequest(
            paymentConfig.amount,
            paymentConfig.currency,
            paymentConfig.description,
            OtpAuthActivity.RETURN_URL,
            StcPaymentSource(cleanPhoneNumber),
            paymentConfig.metadata ?: HashMap()
        )

        CoroutineScope(Job() + Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val response = _paymentService.create(request)
                    RequestResult.Success(response)
                } catch (e: ApiException) {
                    RequestResult.Failure(e)
                } catch (e: Exception) {
                    RequestResult.Failure(e)
                }
            }

            when (result) {
                is RequestResult.Success -> {
                    payment.value = result.payment
                    when (result.payment.status.lowercase()) {
                        "initiated" -> {
                            Log.d("status value after initiating", status.value.toString())
                            status.value =
                                Status.PaymentOtpSecure(result.payment.getStcPayTransactionUrl())
                        }

                        else -> {
                            _sheetResult.value = PaymentResult.Completed(result.payment)
                        }
                    }
                }

                is RequestResult.Failure -> {
                    _sheetResult.value = PaymentResult.Failed(result.e)
                }
            }
        }
    }


    fun verifyOTP() {
        Log.d("otp status: ", payment.value?.status.toString())

        CoroutineScope(Job() + Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val response = _paymentService.getResponse(
                        payment.value!!.getStcPayTransactionUrl(),
                        otpValue
                    )
                    RequestResult.Success(response)
                } catch (e: ApiException) {
                    RequestResult.Failure(e)
                } catch (e: Exception) {
                    RequestResult.Failure(e)
                }
            }

            when (result) {
                is RequestResult.Success -> {
                    status.value = Status.VerifyOtp
                    payment.value = result.payment
                    when (result.payment.status.lowercase()) {
                        "paid" -> {
                            Log.d("status value after paying", status.value.toString())
                        }

                        else -> {
                            _sheetResult.value = PaymentResult.Completed(result.payment)
                        }
                    }
                }

                is RequestResult.Failure -> {
                    _sheetResult.value = PaymentResult.Failed(result.e)
                }
            }
        }
    }

//    fun verifyOTP() {
//        Log.d("otp status: ", payment.value?.status.toString())
//
//        CoroutineScope(Job() + Dispatchers.Main).launch {
//            payment.value = withContext(Dispatchers.IO) {
//                _paymentService.getResponse(
//                    payment.value!!.getStcPayTransactionUrl(), otpValue
//                )
//
//                // TODO: handle Exception
//            }
//        }


    var handleOtpLayoutVisibility: Int
        get() {
            val visibility = when (status.value) {
                is Status.PaymentOtpSecure -> View.VISIBLE
                else -> View.GONE
            }
            return visibility
        }
        set(value) {}
    var handleStcLayoutVisibility: Int
        get() {
            val visibility = when (status.value) {
                is Status.Reset -> View.VISIBLE
                else -> View.GONE
            }
            return visibility
        }
        set(value) {}

    fun onPaymentAuthReturn(result: OtpAuthActivity.AuthResult) {
        when (result) {
            is OtpAuthActivity.AuthResult.Completed -> {
                if (result.id != payment.value?.id) {
                    throw Exception("Got different ID from auth process ${result.id} instead of ${payment.value?.id}")
                }

                val payment = payment.value!!
                payment.apply {
                    status = result.status
                    source["message"] = result.message
                }

                _sheetResult.value = PaymentResult.Completed(payment)
            }

            is OtpAuthActivity.AuthResult.Failed -> {
                _sheetResult.value = PaymentResult.Failed(PaymentSheetException(result.error))
            }

            is OtpAuthActivity.AuthResult.Canceled -> {
                _sheetResult.value = PaymentResult.Canceled
            }
        }
    }

    fun numberTextChanged(textEdit: Editable) {
        if (ccOnChangeLocked) {
            return
        }

        ccOnChangeLocked = true

        val input = textEdit.toString().replace(" ", "")
        val formatted = StringBuilder()

        for ((current, char) in input.toCharArray().withIndex()) {
            if (current > 9) {
                break
            }
            if (current == 2) {
                formatted.append(' ')
            }
            if (current == 6) {
                formatted.append(' ')
            }

            formatted.append(char)
        }

        textEdit.replace(0, textEdit.length, formatted.toString())

        ccOnChangeLocked = false
    }


    sealed class Status : Parcelable {
        @Parcelize
        object Reset : Status()

        @Parcelize
        object SubmittingPayment : Status()

        @Parcelize
        data class PaymentOtpSecure(val url: String) : Status()

        @Parcelize
        object VerifyOtp : Status()

        @Parcelize
        data class Failure(val e: Throwable) : Status()
    }

    internal sealed class RequestResult {
        data class Success(val payment: Payment) : RequestResult()
        data class Failure(val e: Exception) : RequestResult()
    }
}
