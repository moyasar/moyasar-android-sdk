package com.moyasar.android.sdk.creditcard.presentation.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.text.Editable
import com.moyasar.android.sdk.core.exceptions.PaymentSheetException
import com.moyasar.android.sdk.core.extensions.default
import com.moyasar.android.sdk.core.extensions.distinctUntilChanged
import com.moyasar.android.sdk.core.extensions.scope
import com.moyasar.android.sdk.core.util.CreditCardFormatter
import com.moyasar.android.sdk.core.util.getFormattedAmount
import com.moyasar.android.sdk.core.util.parseExpiry
import com.moyasar.android.sdk.creditcard.data.models.sources.CardPaymentSource
import com.moyasar.android.sdk.core.data.response.PaymentResponse
import com.moyasar.android.sdk.creditcard.data.models.request.PaymentRequest
import com.moyasar.android.sdk.creditcard.data.models.request.TokenRequest
import com.moyasar.android.sdk.stcpay.data.models.sources.STCPayPaymentSource
import com.moyasar.android.sdk.core.domain.entities.PaymentResult
import com.moyasar.android.sdk.creditcard.domain.usecases.CreatePaymentUseCase
import com.moyasar.android.sdk.creditcard.domain.usecases.CreateTokenUseCase
import com.moyasar.android.sdk.stcpay.domain.usecases.ValidateSTCPayOTPUseCase
import com.moyasar.android.sdk.creditcard.presentation.model.AuthResultViewState
import com.moyasar.android.sdk.creditcard.presentation.model.FieldValidation
import com.moyasar.android.sdk.creditcard.presentation.model.PaymentConfig
import com.moyasar.android.sdk.creditcard.presentation.model.PaymentStatusViewState
import com.moyasar.android.sdk.creditcard.presentation.model.RequestResultViewState
import com.moyasar.android.sdk.creditcard.presentation.view.fragments.PaymentAuthFragment
import com.moyasar.android.sdk.stcpay.presentation.model.STCPayViewState
import com.moyasar.android.sdk.stcpay.presentation.model.formatter.SaudiPhoneNumberFormatter
import com.moyasar.android.sdk.stcpay.presentation.model.validation.STCPayFormValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class PaymentSheetViewModel(
    application: Application,
    private val paymentConfig: PaymentConfig,
    private val callback: (PaymentResult) -> Unit,
    internal val formValidator: FormValidator,
    internal val stcPayFormValidator: STCPayFormValidator,
    private val createPaymentUseCase: CreatePaymentUseCase,
    private val createTokenUseCase: CreateTokenUseCase,
    private val validateSTCPayOTPUseCase: ValidateSTCPayOTPUseCase,
) : AndroidViewModel(application) {


    var isFirstVisitEnterMobileNumber = true
    var isFirstVisitOTP: Boolean = true
    private var ccOnChangeLocked = false
    private var mobileNumberOnChangeLocked = false
    private var ccExpiryOnChangeLocked = false

    private val _creditCardStatus =
        MutableLiveData<PaymentStatusViewState>().default(PaymentStatusViewState.Reset)

    private val _stcPayStatus =
        MutableLiveData<STCPayViewState>().default(STCPayViewState.Init)
    private val _payment = MutableLiveData<PaymentResponse?>()

    internal val payment: LiveData<PaymentResponse?> = _payment
    val creditCardStatus: LiveData<PaymentStatusViewState> = _creditCardStatus
    val stcPayStatus: LiveData<STCPayViewState> = _stcPayStatus
    val isFormValid: LiveData<Boolean> = formValidator._isFormValid.distinctUntilChanged()
    val isSTCPayFormValid: LiveData<Boolean> = stcPayFormValidator._isSTCPayFormValid.distinctUntilChanged()


    private val cleanCardNumber: String
        get() = formValidator.number.value!!.replace(" ", "")

    private val cleanMobileNumber: String
        get() = stcPayFormValidator.mobileNumber.value!!.replace(" ", "")

    private val expiryMonth: String
        get() = parseExpiry(formValidator.expiry.value ?: "")?.month.toString()

    private val expiryYear: String
        get() = parseExpiry(formValidator.expiry.value ?: "")?.year.toString()

    // Done logic like this to replicate iOS SDK's behavior
    val amountLabel: String
        get() = getFormattedAmount(paymentConfig)

    private fun notifyPaymentResult(paymentResult: PaymentResult) = callback(paymentResult)

    /*************************
     * Perform Create payment Request After submit button clicked and createSaveOnlyToken = false
     * or After STC Pay Button Clicked
     ************************/
    internal fun createPayment(
        request: PaymentRequest = PaymentRequest(
            amount = paymentConfig.amount,
            currency = paymentConfig.currency,
            description = paymentConfig.description,
            callbackUrl = PaymentAuthFragment.RETURN_URL,
            source = CardPaymentSource(
                formValidator.name.value!!,
                cleanCardNumber,
                expiryMonth,
                expiryYear,
                formValidator.cvc.value!!,
                if (paymentConfig.manual) "true" else "false",
                if (paymentConfig.saveCard) "true" else "false",
            ),
            metadata = paymentConfig.metadata ?: HashMap()
        ),
        mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) {
        scope(
            mainDispatcher = mainDispatcher,
            ioDispatcher = ioDispatcher,
            block = { createPaymentUseCase(request) }) { result ->
            when (result) {
                is RequestResultViewState.Success -> {
                    _payment.value = result.data

                    when (result.data.status.lowercase()) {
                        "initiated" -> {
                            if (result.data.source["type"].equals("creditcard"))
                                _creditCardStatus.value =
                                    PaymentStatusViewState.PaymentAuth3dSecure(result.data.getCardTransactionUrl())
                            else {
                                _stcPayStatus.value =
                                    STCPayViewState.STCPayOTPAuth(result.data.getSTCPayTransactionUrl())
                            }
                        }

                        else -> {
                            notifyPaymentResult(PaymentResult.Completed(result.data))
                        }
                    }
                }

                is RequestResultViewState.Failure -> {
                    notifyPaymentResult(PaymentResult.Failed(result.e))
                }
            }
        }
    }

    /*************************
     * Perform Create Save only Token Request After submit button clicked and createSaveOnlyToken = true
     ************************/
    internal fun createSaveOnlyToken(
        request: TokenRequest = TokenRequest(
            formValidator.name.value!!,
            cleanCardNumber,
            formValidator.cvc.value!!,
            expiryMonth,
            expiryYear,
            true,
            PaymentAuthFragment.RETURN_URL
        ),
        mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) {
        scope(
            mainDispatcher = mainDispatcher,
            ioDispatcher = ioDispatcher,
            block = { createTokenUseCase(request) }) { result ->
            when (result) {
                is RequestResultViewState.Success -> {
                    val data = result.data
                    notifyPaymentResult(PaymentResult.CompletedToken(data))
                }

                is RequestResultViewState.Failure -> {
                    notifyPaymentResult(PaymentResult.Failed(result.e))
                }
            }
        }
    }

    internal fun onPaymentAuthReturn(result: AuthResultViewState) {
        when (result) {
            is AuthResultViewState.Completed -> {
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

            is AuthResultViewState.Failed -> {
                notifyPaymentResult(PaymentResult.Failed(PaymentSheetException(result.error)))
            }

            is AuthResultViewState.Canceled -> {
                notifyPaymentResult(PaymentResult.Canceled)
            }
        }
    }

    internal fun validateField(fieldType: FieldValidation, hasFocus: Boolean) {
        when (fieldType) {
            FieldValidation.Name -> formValidator.nameValidator.onFieldFocusChange(hasFocus)
            FieldValidation.Number -> formValidator.numberValidator.onFieldFocusChange(hasFocus)
            FieldValidation.Cvc -> formValidator.cvcValidator.onFieldFocusChange(hasFocus)
            FieldValidation.Expiry -> formValidator.expiryValidator.onFieldFocusChange(hasFocus)
        }
    }

    internal fun creditCardNameChanged() {
        formValidator.validate(false)
    }

    internal fun creditCardNumberChanged(textEdit: Editable) {
        if (ccOnChangeLocked) {
            return
        }
        ccOnChangeLocked = true
        val formatted = CreditCardFormatter.formatCardNumber(textEdit.toString())
        textEdit.replace(0, textEdit.length, formatted)
        formValidator.validate(false)
        ccOnChangeLocked = false
    }

    internal fun mobileNumberChanged(textEdit: Editable) {
        if (mobileNumberOnChangeLocked) {
            return
        }
        mobileNumberOnChangeLocked = true
        // todo test
        val formatted = SaudiPhoneNumberFormatter.formatPhoneNumber(textEdit.toString())
        textEdit.replace(0, textEdit.length, formatted)
        stcPayFormValidator.validateSTCMobile(true)
        mobileNumberOnChangeLocked = false
    }


    internal fun creditCardExpiryChanged(textEdit: Editable) {
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

        formValidator.validate(false)

        ccExpiryOnChangeLocked = false
    }

    internal fun creditCardCvcChanged() {
        formValidator.validate(false)
    }

    internal fun submit() {
        if (!formValidator.validate()) {
            return
        }

        if (_creditCardStatus.value != PaymentStatusViewState.Reset) {
            return
        }

        _creditCardStatus.value = PaymentStatusViewState.SubmittingPayment

        if (paymentConfig.createSaveOnlyToken) {
            createSaveOnlyToken()
        } else {
            createPayment()
        }
    }

    internal fun submitSTC() {
        if (!stcPayFormValidator.validateSTCMobile()) {
            return
        }

        _stcPayStatus.value = STCPayViewState.SubmittingSTCPayMobileNumber
        createPayment(
            request = PaymentRequest(
                amount = paymentConfig.amount,
                currency = paymentConfig.currency,
                description = paymentConfig.description,
                callbackUrl = PaymentAuthFragment.RETURN_URL,
                source = STCPayPaymentSource(
                    mobile = cleanMobileNumber,
                ),
                metadata = paymentConfig.metadata ?: HashMap()
            )
        )

    }

    fun submitSTCPayOTP(
        transactionURL: String, otp: String, mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) {
        _stcPayStatus.value= STCPayViewState.SubmittingSTCPayOTP
        scope(
            mainDispatcher = mainDispatcher,
            ioDispatcher = ioDispatcher,
            block = {
                validateSTCPayOTPUseCase(
                    transactionURL = transactionURL,
                    otp = otp
                )
            }) { result ->
            when (result) {
                is RequestResultViewState.Success -> {
                    _payment.value = result.data

                    when (result.data.status.lowercase()) {
                        "initiated" -> {
                            _stcPayStatus.value =
                                    STCPayViewState.STCPayOTPAuth(result.data.getSTCPayTransactionUrl())

                        }

                        else -> {
                            notifyPaymentResult(PaymentResult.Completed(result.data))
                        }
                    }
                }

                is RequestResultViewState.Failure -> {
                    notifyPaymentResult(PaymentResult.Failed(result.e))
                }
            }
        }
    }

    fun stcPayOTPChanged() {
        stcPayFormValidator.validateSTCOTP()
    }
}
