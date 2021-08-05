package com.moyasar.android.sdk.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moyasar.android.sdk.PaymentConfig
import com.moyasar.android.sdk.payment.Payment
import com.moyasar.android.sdk.payment.PaymentService
import com.moyasar.android.sdk.payment.RetrofitFactory
import com.moyasar.android.sdk.payment.models.CardPaymentSource
import com.moyasar.android.sdk.payment.models.PaymentRequest
import com.moyasar.android.sdk.ui.PaymentAuthActivity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                _status.value = Status.Idle
                _uiStatus.value = UiStatus.RuntimeError(throwable as Exception)
            }
            else -> throw throwable
        }
    }

    private val _uiStatus = MutableLiveData<UiStatus>(UiStatus.Ok)
    private val _status = MutableLiveData(Status.Idle)
    private val _payment = MutableLiveData<Payment?>(null)
    private val _errors = MutableLiveData<String?>(null)

    internal val uiStatus: LiveData<UiStatus> = _uiStatus
    internal val status: LiveData<Status> = _status
    internal val payment: LiveData<Payment?> = _payment
    internal val errors: LiveData<String?> = _errors

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
        if (_status.value != Status.Idle) {
            // TODO: Show error, or just quit quietly
            return
        }

        _status.value = Status.SubmittingPayment

        // Take params
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
                    if (!response.isSuccessful) {
                        throw HttpException(response)
                    }
                    RequestResult.Success(response.body())
                } catch (e: Exception) {
                    RequestResult.Failure(e)
                }
            }

            when (result) {
                is RequestResult.Success -> {
                    val payment = result.payment!!
                    _payment.value = payment

                    when (payment.status.lowercase()) {
                        "initiated" -> _status.value = Status.PaymentAuth3dSecure
                        "paid", "authorized" -> _status.value = Status.Finish
                        else -> {
                            _status.value = Status.Idle
                            _payment.value = null
                            _uiStatus.value = UiStatus.RuntimeError(InvalidPaymentException(payment))
                        }
                    }
                }
                is RequestResult.Failure -> {
                    _status.value = Status.Idle
                    _uiStatus.value = UiStatus.RuntimeError(result.e)
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

                _payment.value?.apply {
                    status = result.status
                    source["message"] = result.message
                }

                _status.value = Status.Finish
            }
            is PaymentAuthActivity.AuthResult.Failed -> {
                _payment.value = null
                _status.value = Status.Idle
                _uiStatus.value = UiStatus.RuntimeError(RuntimeException(result.error ?: "Unknown error"))
            }
            is PaymentAuthActivity.AuthResult.Canceled -> {
                _payment.value = null
                _status.value = Status.Idle
                _uiStatus.value = UiStatus.RuntimeError(RuntimeException("User canceled"))
            }
        }
    }

    internal enum class Status {
        Idle,
        SubmittingPayment,
        PaymentAuth3dSecure,
        Finish,
    }

    internal sealed class UiStatus {
        object Ok : UiStatus()
        data class RuntimeError(val e: Exception) : UiStatus()
    }

    internal sealed class RequestResult {
        data class Success(val payment: Payment?) : RequestResult()
        data class Failure(val e: Exception) : RequestResult()
    }

    internal class InvalidPaymentException(val payment: Payment) : RuntimeException()
}
