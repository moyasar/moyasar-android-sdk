package com.moyasar.android.sdk.data

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.Transformations
import com.moyasar.android.sdk.PaymentConfig
import com.moyasar.android.sdk.PaymentResult
import com.moyasar.android.sdk.payment.models.Payment
import com.moyasar.android.sdk.payment.PaymentService
import com.moyasar.android.sdk.payment.RetrofitFactory
import com.moyasar.android.sdk.payment.models.CardPaymentSource
import com.moyasar.android.sdk.payment.models.PaymentRequest
import com.moyasar.android.sdk.ui.PaymentAuthActivity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import retrofit2.HttpException
import java.io.IOException
import java.lang.Exception
import java.lang.RuntimeException
import java.util.*

class PaymentSheetViewModel(
    private val paymentConfig: PaymentConfig
) : ViewModel() {
    private val _paymentService: PaymentService by lazy {
        RetrofitFactory(paymentConfig.baseUrl, paymentConfig.apiKey)
            .build()
            .create(PaymentService::class.java)
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        when (throwable) {
            is IOException, is HttpException, is RuntimeException -> {
                _status.value = Status.Failure(throwable)
            }
            else -> throw throwable
        }
    }

    private val _status = MutableLiveData<Status>(Status.Reset)
    private val _payment = MutableLiveData<Payment?>(null)
    private val _errors = MutableLiveData<String?>(null)
    private val _sheetResult = MutableLiveData<PaymentResult?>(null)

    internal val status: LiveData<Status> = _status
    internal val payment: LiveData<Payment?> = _payment
    internal val errors: LiveData<String?> = _errors
    internal val sheetResult: LiveData<PaymentResult?> = Transformations.distinctUntilChanged(_sheetResult)

    val name = MutableLiveData("Ali H")
    val number = MutableLiveData("4111111111111111")
    val cvc = MutableLiveData("123")
    val monthSelectedPos = MutableLiveData(0)
    val yearSelectedPos = MutableLiveData(3)

    val futureYears: List<String> by lazy {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        (year..(year + 15)).map { it.toString() }.toList()
    }

    val selectedMonth: String
        get() = (monthSelectedPos.value!! + 1).toString()

    val selectedYear: String
        get() = futureYears[yearSelectedPos.value!!]

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
            CardPaymentSource(name.value!!, number.value!!, selectedMonth, selectedYear, cvc.value!!)
        )

        viewModelScope.launch(exceptionHandler) {
            val result = withContext(Dispatchers.IO) {
                try {
                    val response = _paymentService.create(request)
                    if (response.isSuccessful) RequestResult.Success(response.body()) else
                        RequestResult.Failure(HttpException(response))
                } catch (e: Exception) {
                    RequestResult.Failure(e)
                }
            }

            when (result) {
                is RequestResult.Success -> {
                    val payment = result.payment!!
                    _payment.value = payment

                    when (payment.status.lowercase()) {
                        "initiated" -> {
                            _status.value = Status.PaymentAuth3dSecure(payment.getCardTransactionUrl())
                        }
                        else -> {
                            _sheetResult.value = PaymentResult.Completed(payment)
                        }
                    }
                }
                is RequestResult.Failure -> {
                    _status.value = Status.Failure(result.e)
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
                _sheetResult.value = PaymentResult.Failed(result.error)
            }
            is PaymentAuthActivity.AuthResult.Canceled -> {
                _sheetResult.value = PaymentResult.Canceled
            }
        }
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
        data class Success(val payment: Payment?) : RequestResult()
        data class Failure(val e: Exception) : RequestResult()
    }
}
