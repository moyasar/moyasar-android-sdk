package com.moyasar.android.sdk.presentation.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.os.Parcelable
import android.text.Editable
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.core.exceptions.ApiException
import com.moyasar.android.sdk.core.exceptions.PaymentSheetException
import com.moyasar.android.sdk.core.extensions.default
import com.moyasar.android.sdk.core.extensions.distinctUntilChanged
import com.moyasar.android.sdk.data.models.CardPaymentSource
import com.moyasar.android.sdk.data.models.Payment
import com.moyasar.android.sdk.data.models.PaymentRequest
import com.moyasar.android.sdk.data.models.TokenRequest
import com.moyasar.android.sdk.presentation.fragments.PaymentAuthFragment
import com.moyasar.android.sdk.core.util.CreditCardNetwork
import com.moyasar.android.sdk.core.util.getNetwork
import com.moyasar.android.sdk.core.util.isValidLuhnNumber
import com.moyasar.android.sdk.core.util.parseExpiry
import com.moyasar.android.sdk.core.util.LiveDataValidator
import com.moyasar.android.sdk.domain.entities.PaymentConfig
import com.moyasar.android.sdk.domain.entities.PaymentResult
import com.moyasar.android.sdk.domain.usecases.CreatePaymentUseCase
import com.moyasar.android.sdk.domain.usecases.CreateTokenUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import java.text.DecimalFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.pow

class PaymentSheetViewModel(
    private val application: Application,
    private val paymentConfig: PaymentConfig,
    private val callback: (PaymentResult) -> Unit,
) : AndroidViewModel(application) {
    private val createPaymentUseCase by lazy { CreatePaymentUseCase(paymentConfig.apiKey, paymentConfig.baseUrl) }
    private val createTokenUseCase by lazy { CreateTokenUseCase(paymentConfig.apiKey, paymentConfig.baseUrl) }

    private var ccOnChangeLocked = false
    private var ccExpiryOnChangeLocked = false

    private val _status = MutableLiveData<Status>().default(Status.Reset)
    private val _payment = MutableLiveData<Payment?>()
    private val _isFormValid = MediatorLiveData<Boolean>().default(false)

    internal val payment: LiveData<Payment?> = _payment
    val status: LiveData<Status> = _status
    val isFormValid: LiveData<Boolean> = _isFormValid.distinctUntilChanged()

    val name = MutableLiveData<String>().default("")
    val number = MutableLiveData<String>().default("")
    val cvc = MutableLiveData<String>().default("")
    val expiry = MutableLiveData<String>().default("")

    val nameValidator = LiveDataValidator(name).apply {
        val latinRegex = Regex("^[a-zA-Z\\-\\s]+\$")
        val nameRegex = Regex("^[a-zA-Z\\-]+\\s+?([a-zA-Z\\-]+\\s?)+\$")

        addRule(application.getString(R.string.name_is_required)) { it.isNullOrBlank() }
        addRule(application.getString(R.string.only_english_alpha)) { !latinRegex.matches(it ?: "") }
        addRule(application.getString(R.string.both_names_required)) { !nameRegex.matches(it ?: "") }
    }

    val numberValidator = LiveDataValidator(number).apply {
        addRule(application.getString(R.string.card_number_required)) { it.isNullOrBlank() }
        addRule(application.getString(R.string.invalid_card_number)) { !isValidLuhnNumber(it ?: "") }
        addRule(application.getString(R.string.unsupported_network)) {
            getNetwork(
                it ?: ""
            ) == CreditCardNetwork.Unknown
        }
    }

    val cvcValidator = LiveDataValidator(cvc).apply {
        addRule(application.getString(R.string.cvc_required)) { it.isNullOrBlank() }
        addRule(application.getString(R.string.invalid_cvc)) {
            when (getNetwork(number.value ?: "")) {
                CreditCardNetwork.Amex -> (it?.length ?: 0) < 4
                else -> (it?.length ?: 0) < 3
            }
        }
    }

    val expiryValidator = LiveDataValidator(expiry).apply {
        addRule(application.getString(R.string.expiry_is_required)) { it.isNullOrBlank() }
        addRule(application.getString(R.string.invalid_expiry)) { parseExpiry(it ?: "")?.isInvalid() ?: true }
        addRule(application.getString(R.string.expired_card)) { parseExpiry(it ?: "")?.expired() ?: false }
    }

    private val cleanCardNumber: String
        get() = number.value!!.replace(" ", "")

    private val expiryMonth: String
        get() = parseExpiry(expiry.value ?: "")?.month.toString()

    private val expiryYear: String
        get() = parseExpiry(expiry.value ?: "")?.year.toString()

    // Done logic like this to replicate iOS SDK's behavior
    val amountLabel: String
        get() {
            val currentLocale = Locale.getDefault()
            val paymentCurrency = Currency.getInstance(paymentConfig.currency)

            val numberFormatter = DecimalFormat.getNumberInstance(Locale.US).apply {
                minimumFractionDigits = paymentCurrency.defaultFractionDigits
                isGroupingUsed = true
            }

            val currencyFormatter = DecimalFormat.getCurrencyInstance(currentLocale).apply {
                currency = paymentCurrency
            }

            val amount =
                paymentConfig.amount / (10.0.pow(currencyFormatter.currency!!.defaultFractionDigits.toDouble()))
            val formattedNumber = numberFormatter.format(amount)
            val currencySymbol = currencyFormatter.currency!!.symbol

            return if (currentLocale.language == "ar") {
                "$formattedNumber $currencySymbol"
            } else {
                "$currencySymbol $formattedNumber"
            }
        }

    private fun validateForm(isShowError: Boolean = true): Boolean {
        val validators = listOf(nameValidator, numberValidator, cvcValidator, expiryValidator)
        return if (isShowError) {
            validators.all { it.isValid() }.also { _isFormValid.value = it }
        } else {
            validators.all { it.isValidWithoutErrorMessage() }.also { _isFormValid.value = it }
        }
    }

    private fun notifyPaymentResult(paymentResult: PaymentResult) {
        callback(paymentResult)
    }

    private fun createPayment() {
        val request = PaymentRequest(
            paymentConfig.amount,
            paymentConfig.currency,
            paymentConfig.description,
            PaymentAuthFragment.RETURN_URL,
            CardPaymentSource(
                name.value!!,
                cleanCardNumber,
                expiryMonth,
                expiryYear,
                cvc.value!!,
                if (paymentConfig.manual) "true" else "false",
                if (paymentConfig.saveCard) "true" else "false",
            ),
            paymentConfig.metadata ?: HashMap()
        )

        CoroutineScope(Job() + Dispatchers.Main)
            .launch {
                val result = withContext(Dispatchers.IO) {
                    try {
                        val response = createPaymentUseCase(request)
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
                                notifyPaymentResult(PaymentResult.Completed(result.payment))
                            }
                        }
                    }

                    is RequestResult.Failure -> {
                        notifyPaymentResult(PaymentResult.Failed(result.e))
                    }
                }
            }
    }

    private fun createSaveOnlyToken() {
        val request = TokenRequest(
            name.value!!,
            cleanCardNumber,
            cvc.value!!,
            expiryMonth,
            expiryYear,
            true,
            "https://sdk.moyasar.com"
        )

        CoroutineScope(Job() + Dispatchers.Main).launch {
            notifyPaymentResult(
                try {
                    PaymentResult.CompletedToken(createTokenUseCase(request))
                } catch (e: ApiException) {
                    PaymentResult.Failed(e)
                } catch (e: Exception) {
                    PaymentResult.Failed(e)
                }
            )
        }
    }

    internal fun onPaymentAuthReturn(result: PaymentAuthFragment.AuthResult) {
        when (result) {
            is PaymentAuthFragment.AuthResult.Completed -> {
                if (result.id != _payment.value?.id) {
                    throw Exception("Got different ID from auth process ${result.id} instead of ${_payment.value?.id}")
                }

                val payment = _payment.value!!
                payment.apply {
                    status = result.status
                    source["message"] = result.message
                }

                notifyPaymentResult(PaymentResult.Completed(payment))
            }

            is PaymentAuthFragment.AuthResult.Failed -> {
                notifyPaymentResult(PaymentResult.Failed(PaymentSheetException(result.error)))
            }

            is PaymentAuthFragment.AuthResult.Canceled -> {
                notifyPaymentResult(PaymentResult.Canceled)
            }
        }
    }

    fun validateField(fieldType: FieldValidation, hasFocus: Boolean) {
        when (fieldType) {
            FieldValidation.Name -> nameValidator.onFieldFocusChange(hasFocus)
            FieldValidation.Number -> numberValidator.onFieldFocusChange(hasFocus)
            FieldValidation.Cvc -> cvcValidator.onFieldFocusChange(hasFocus)
            FieldValidation.Expiry -> expiryValidator.onFieldFocusChange(hasFocus)
        }
    }

    fun submit() {
        if (!validateForm()) {
            return
        }

        if (_status.value != Status.Reset) {
            return
        }

        _status.value = Status.SubmittingPayment

        if (paymentConfig.createSaveOnlyToken) {
            createSaveOnlyToken()
        } else {
            createPayment()
        }
    }

    fun creditCardNameChanged() {
        validateForm(false)
    }

    fun creditCardNumberChanged(textEdit: Editable) {
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

        validateForm(false)

        ccOnChangeLocked = false
    }

    fun creditCardExpiryChanged(textEdit: Editable) {
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

        validateForm(false)

        ccExpiryOnChangeLocked = false
    }

    fun creditCardCvcChanged() {
        validateForm(false)
    }

    sealed class Status : Parcelable {
        @Parcelize
        data object Reset : Status()

        @Parcelize
        data object SubmittingPayment : Status()

        @Parcelize
        data class PaymentAuth3dSecure(val url: String) : Status()
    }

    internal sealed class RequestResult {
        data class Success(val payment: Payment) : RequestResult()
        data class Failure(val e: Exception) : RequestResult()
    }

    enum class FieldValidation {
        Name,
        Number,
        Expiry,
        Cvc
    }
}
internal object SharedPaymentViewModelHolder {
    lateinit var sharedViewModel: PaymentSheetViewModel
}