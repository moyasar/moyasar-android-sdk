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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.lang.Exception
import java.lang.RuntimeException

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

    val name = MutableLiveData("")
    val number = MutableLiveData("")
    val month = MutableLiveData("")
    val year = MutableLiveData("")
    val cvc = MutableLiveData("")

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
            "https://sdk.moyasar.com/payment/return",
            CardPaymentSource(name.value!!, number.value!!, month.value!!, year.value!!, cvc.value!!)
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

        // If we get 4xx, show error to user and abort
        // If we get 5xx, show error to user and abort
        // If we get a network error show error and prompt to try again
        // If we get a 201 with payment initiated, show 3D secure page
        // If we get authorized or paid, then just close sheet and return completed with payment object
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
