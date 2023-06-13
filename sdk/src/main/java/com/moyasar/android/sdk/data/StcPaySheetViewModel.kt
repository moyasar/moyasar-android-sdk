package com.moyasar.android.sdk.data

import android.content.res.Resources
import android.os.Parcelable
import android.text.Editable
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
import com.moyasar.android.sdk.ui.PaymentAuthActivity
import com.moyasar.android.sdk.util.CreditCardNetwork
import com.moyasar.android.sdk.util.getNetwork
import com.moyasar.android.sdk.util.isValidLuhnNumber
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
    private val paymentConfig: PaymentConfig,
    private val resources: Resources
) : ViewModel() {

    private val _paymentService: PaymentService by lazy {
        PaymentService(paymentConfig.apiKey, paymentConfig.baseUrl)
    }

    private var ccOnChangeLocked = false

    private val _status = MutableLiveData<Status>(Status.Reset)
    private val _payment = MutableLiveData<Payment?>(null)
    private val _sheetResult = MutableLiveData<PaymentResult?>(null)
    private val _isFormValid = MediatorLiveData<Boolean>()

    val status: LiveData<Status> = _status
    internal val payment: LiveData<Payment?> = _payment
    internal val sheetResult: LiveData<PaymentResult?> =
        _sheetResult.distinctUntilChanged()

    val number = MutableLiveData("")


    val numberValidator = LiveDataValidator(number).apply {
        addRule("Phone number is required") { it.isNullOrBlank() }
        addRule("Phone number is invalid") { !isValidLuhnNumber(it ?: "") }
        addRule("Unsupported phone number network") {
            getNetwork(
                it ?: ""
            ) == CreditCardNetwork.Unknown
        }
    }


    fun validateForm(): Boolean {
        return numberValidator.isValid().also { _isFormValid.value = it }
    }

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
                    10.0,
                    formatter.currency!!.defaultFractionDigits.toDouble()
                ))
            )

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
                    10.0,
                    formatter.currency!!.defaultFractionDigits.toDouble()
                ))
            )
        }

    fun submit() {
        if (!validateForm()) {
            return;
        }

        if (_status.value != Status.Reset) {
            return
        }

        _status.value = Status.SubmittingPayment

        val request = PaymentRequest(
            paymentConfig.amount,
            paymentConfig.currency,
            paymentConfig.description,
            PaymentAuthActivity.RETURN_URL,
            StcPaymentSource(cleanPhoneNumber),
            paymentConfig.metadata ?: HashMap()
        )

        CoroutineScope(Job() + Dispatchers.Main)
            .launch {
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
                        _payment.value = result.payment

                        when (result.payment.status.lowercase()) {
                            "initiated" -> {
                                _status.value =
                                    Status.PaymentOTPSecure(result.payment.getStcPayTransactionUrl())
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

    fun onPaymentAuthReturn(result: PaymentAuthActivity.AuthResult) {
        when (result) {
            is PaymentAuthActivity.AuthResult.Completed -> {
                if (result.id != _payment.value?.id) {
                    throw Exception("Got different ID from auth process ${result.id} instead of ${_payment.value?.id}")
                }

                val payment = _payment.value!!
                payment.apply {
                    status = result.status
                    source["message"] = result.message
                }

                _sheetResult.value = PaymentResult.Completed(payment)
            }

            is PaymentAuthActivity.AuthResult.Failed -> {
                _sheetResult.value = PaymentResult.Failed(PaymentSheetException(result.error))
            }

            is PaymentAuthActivity.AuthResult.Canceled -> {
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
            if (current > 10) {
                break
            }

            if (current > 0 && current % 3 == 0) {
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
        data class PaymentOTPSecure(val url: String) : Status()

        @Parcelize
        data class Failure(val e: Throwable) : Status()
    }

    internal sealed class RequestResult {
        data class Success(val payment: Payment) : RequestResult()
        data class Failure(val e: Exception) : RequestResult()
    }
}
