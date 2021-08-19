package com.moyasar.android.sdk.data

import android.os.Parcelable
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.Transformations
import com.moyasar.android.sdk.PaymentConfig
import com.moyasar.android.sdk.PaymentResult
import com.moyasar.android.sdk.exceptions.ApiException
import com.moyasar.android.sdk.exceptions.PaymentSheetException
import com.moyasar.android.sdk.payment.models.Payment
import com.moyasar.android.sdk.payment.PaymentService
import com.moyasar.android.sdk.payment.models.CardPaymentSource
import com.moyasar.android.sdk.payment.models.PaymentRequest
import com.moyasar.android.sdk.ui.PaymentAuthActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import java.lang.Exception
import java.text.NumberFormat
import java.util.*

class PaymentSheetViewModel(
    private val paymentConfig: PaymentConfig
) : ViewModel() {
    private val _paymentService: PaymentService by lazy {
        PaymentService(paymentConfig.apiKey, paymentConfig.baseUrl)
    }

    private var ccOnChangeLocked = false
    private var ccExpiryOnChangeLocked = false

    private val _status = MutableLiveData<Status>(Status.Reset)
    private val _payment = MutableLiveData<Payment?>(null)
    private val _errors = MutableLiveData<String?>(null)
    private val _sheetResult = MutableLiveData<PaymentResult?>(null)

    internal val status: LiveData<Status> = _status
    internal val payment: LiveData<Payment?> = _payment
    internal val sheetResult: LiveData<PaymentResult?> = Transformations.distinctUntilChanged(_sheetResult)

    val name = MutableLiveData("Ali H")
    val number = MutableLiveData("4111111111111111")
    val cvc = MutableLiveData("123")
    val expiry = MutableLiveData("09 / 25")

    val cleanCardNumber: String
        get() = number.value!!.replace(" ", "")

    val expiryMonth: String
        get() = ""

    val expiryYear: String
        get() = ""

    val currency: String
        get() = paymentConfig.currency.uppercase()

    val formattedAmount: String
        get() {
            val formatter = NumberFormat.getInstance()
            formatter.currency = Currency.getInstance(paymentConfig.currency)
            formatter.minimumFractionDigits = formatter.currency!!.defaultFractionDigits
            return formatter.format(paymentConfig.amount / (Math.pow(10.0,
                formatter.currency!!.defaultFractionDigits.toDouble()
            )))
        }

    fun submit() {
        if (_status.value != Status.Reset) {
            return
        }

        _status.value = Status.SubmittingPayment

        val request = PaymentRequest(
            paymentConfig.amount,
            paymentConfig.currency,
            paymentConfig.description,
            PaymentAuthActivity.RETURN_URL,
            CardPaymentSource(name.value!!, cleanCardNumber, expiryMonth, expiryYear, cvc.value!!)
        )

        viewModelScope.launch {
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
                            _status.value = Status.PaymentAuth3dSecure(result.payment.getCardTransactionUrl())
                        }
                        else -> {
                            _sheetResult.value = PaymentResult.Completed(result.payment)
                        }
                    }
                }
                is RequestResult.Failure -> {
                    if (result.e is ApiException && result.e.response.type == "invalid_request_error") {
                        _status.value = Status.Failure(result.e)
                    } else {
                        _sheetResult.value = PaymentResult.Failed(result.e)
                    }
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

    fun creditCardTextChanged(textEdit: Editable) {
        if (ccOnChangeLocked) {
            return
        }

        ccOnChangeLocked = true

        val input = textEdit.toString().replace(" ", "")
        val formatted = StringBuilder()

        for ((current, char) in input.toCharArray().withIndex()) {
            if (current > 15) {
                break
            }

            if (current > 0 && current % 4 == 0) {
                formatted.append(' ')
            }

            formatted.append(char)
        }

        textEdit.replace(0, textEdit.length, formatted.toString())

        ccOnChangeLocked = false
    }

    fun expiryChanged(textEdit: Editable) {
        if (ccExpiryOnChangeLocked) {
            return
        }

        ccExpiryOnChangeLocked = true

        val input = textEdit.toString()
            .replace(" ", "")
            .replace("/", "")

        val formatted = StringBuilder()

        for ((current, char) in input.toCharArray().withIndex()) {
            if (current > 5) {
                break
            }

            if (current == 2) {
                formatted.append(" / ")
            }

            formatted.append(char)
        }

        textEdit.replace(0, textEdit.length, formatted.toString())

        ccExpiryOnChangeLocked = false
    }

    internal sealed class Status : Parcelable {
        @Parcelize
        object Reset : Status()

        @Parcelize
        object SubmittingPayment : Status()

        @Parcelize
        data class PaymentAuth3dSecure(val url: String) : Status()

        @Parcelize
        data class Failure(val e: Throwable) : Status()
    }

    internal sealed class RequestResult {
        data class Success(val payment: Payment) : RequestResult()
        data class Failure(val e: Exception) : RequestResult()
    }
}
