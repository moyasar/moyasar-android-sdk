package com.moyasar.android.sdk.data

import android.content.res.Resources
import android.os.Parcelable
import android.text.Editable
import androidx.lifecycle.*
import com.moyasar.android.sdk.PaymentConfig
import com.moyasar.android.sdk.PaymentResult
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.exceptions.ApiException
import com.moyasar.android.sdk.exceptions.PaymentSheetException
import com.moyasar.android.sdk.payment.PaymentService
import com.moyasar.android.sdk.payment.models.CardPaymentSource
import com.moyasar.android.sdk.payment.models.Payment
import com.moyasar.android.sdk.payment.models.PaymentRequest
import com.moyasar.android.sdk.ui.PaymentAuthActivity
import com.moyasar.android.sdk.util.CreditCardNetwork
import com.moyasar.android.sdk.util.getNetwork
import com.moyasar.android.sdk.util.isValidLuhnNumber
import com.moyasar.android.sdk.util.parseExpiry
import kotlinx.coroutines.*
import kotlinx.parcelize.Parcelize
import java.text.NumberFormat
import java.util.*

class PaymentSheetViewModel(
    private val paymentConfig: PaymentConfig,
    private val resources: Resources
) : ViewModel() {

    private val _paymentService: PaymentService by lazy {
        PaymentService(paymentConfig.apiKey, paymentConfig.baseUrl)
    }

    private var ccOnChangeLocked = false
    private var ccExpiryOnChangeLocked = false

    private val _status = MutableLiveData<Status>(Status.Reset)
    private val _payment = MutableLiveData<Payment?>(null)
    private val _sheetResult = MutableLiveData<PaymentResult?>(null)
    private val _isFormValid = MediatorLiveData<Boolean>()

    val status: LiveData<Status> = _status
    internal val payment: LiveData<Payment?> = _payment
    internal val sheetResult: LiveData<PaymentResult?> =
        _sheetResult.distinctUntilChanged()//changed in lifecycle 2.6.0
//        Transformations.distinctUntilChanged(_sheetResult)

    val name = MutableLiveData("")
    val number = MutableLiveData("")
    val cvc = MutableLiveData("")
    val expiry = MutableLiveData("")

    val nameValidator = LiveDataValidator(name).apply {
        val latinRegex = Regex("^[a-zA-Z\\-\\s]+\$")
        val nameRegex = Regex("^[a-zA-Z\\-]+\\s+?([a-zA-Z\\-]+\\s?)+\$")

        addRule("Name is required") { it.isNullOrBlank() }
        addRule("Name should only contain English alphabet") { !latinRegex.matches(it ?: "") }
        addRule("Both first and last names are required") { !nameRegex.matches(it ?: "") }
    }

    val numberValidator = LiveDataValidator(number).apply {
        addRule("Credit card number is required") { it.isNullOrBlank() }
        addRule("Credit card number is invalid") { !isValidLuhnNumber(it ?: "") }
        addRule("Unsupported credit card network") {
            getNetwork(
                it ?: ""
            ) == CreditCardNetwork.Unknown
        }
    }

    val cvcValidator = LiveDataValidator(cvc).apply {
        addRule("Security code is required") { it.isNullOrBlank() }
        addRule("Invalid security code") {
            when (getNetwork(number.value ?: "")) {
                CreditCardNetwork.Amex -> (it?.length ?: 0) < 4
                else -> (it?.length ?: 0) < 3
            }
        }
    }

    val expiryValidator = LiveDataValidator(expiry).apply {
        addRule("Expiry date is required") { it.isNullOrBlank() }
        addRule("Invalid date") { parseExpiry(it ?: "")?.isInvalid() ?: true }
        addRule("Expired card") { parseExpiry(it ?: "")?.expired() ?: false }
    }

    fun validateForm(): Boolean {
        val validators = listOf(nameValidator, numberValidator, cvcValidator, expiryValidator)
        return validators.all { it.isValid() }.also { _isFormValid.value = it }
    }

    val cleanCardNumber: String
        get() = number.value!!.replace(" ", "")

    val expiryMonth: String
        get() = parseExpiry(expiry.value ?: "")?.month.toString()

    val expiryYear: String
        get() = parseExpiry(expiry.value ?: "")?.year.toString()

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
            CardPaymentSource(name.value!!, cleanCardNumber, expiryMonth, expiryYear, cvc.value!!),
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
                                    Status.PaymentAuth3dSecure(result.payment.getCardTransactionUrl())
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

    sealed class Status : Parcelable {
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
