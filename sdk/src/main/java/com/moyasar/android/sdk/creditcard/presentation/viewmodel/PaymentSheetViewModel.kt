package com.moyasar.android.sdk.creditcard.presentation.viewmodel

import android.app.Application
import android.text.Editable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.moyasar.android.sdk.core.data.response.PaymentResponse
import com.moyasar.android.sdk.core.domain.entities.PaymentResult
import com.moyasar.android.sdk.core.exceptions.PaymentSheetException
import com.moyasar.android.sdk.core.extensions.default
import com.moyasar.android.sdk.core.extensions.scope
import com.moyasar.android.sdk.core.util.CreditCardFormatter
import com.moyasar.android.sdk.core.util.getFormattedAmount
import com.moyasar.android.sdk.core.util.parseExpiry
import com.moyasar.android.sdk.creditcard.data.models.request.PaymentRequest
import com.moyasar.android.sdk.creditcard.data.models.request.TokenRequest
import com.moyasar.android.sdk.creditcard.data.models.sources.CardPaymentSource
import com.moyasar.android.sdk.creditcard.domain.usecases.CreatePaymentUseCase
import com.moyasar.android.sdk.creditcard.domain.usecases.CreateTokenUseCase
import com.moyasar.android.sdk.creditcard.presentation.model.AuthResultViewState
import com.moyasar.android.sdk.creditcard.presentation.model.FieldValidation
import com.moyasar.android.sdk.creditcard.presentation.model.InputFieldsUIModel
import com.moyasar.android.sdk.creditcard.presentation.model.PaymentStatusViewState
import com.moyasar.android.sdk.creditcard.presentation.model.RequestResultViewState
import com.moyasar.android.sdk.creditcard.presentation.model.STCPayUIModel
import com.moyasar.android.sdk.creditcard.presentation.utils.getCvcValidationRules
import com.moyasar.android.sdk.creditcard.presentation.utils.getExpiryDateValidationRules
import com.moyasar.android.sdk.creditcard.presentation.utils.getNameValidationRules
import com.moyasar.android.sdk.creditcard.presentation.utils.getNumberValidationRules
import com.moyasar.android.sdk.creditcard.presentation.utils.getOTPValidationRules
import com.moyasar.android.sdk.creditcard.presentation.utils.getPhoneNumberValidationRules
import com.moyasar.android.sdk.creditcard.presentation.utils.isValidCvc
import com.moyasar.android.sdk.creditcard.presentation.utils.isValidExpiryDate
import com.moyasar.android.sdk.creditcard.presentation.utils.isValidName
import com.moyasar.android.sdk.creditcard.presentation.utils.isValidNumber
import com.moyasar.android.sdk.creditcard.presentation.view.fragments.PaymentAuthFragment
import com.moyasar.android.sdk.stcpay.data.models.sources.STCPayPaymentSource
import com.moyasar.android.sdk.stcpay.domain.usecases.ValidateSTCPayOTPUseCase
import com.moyasar.android.sdk.stcpay.presentation.model.STCPayViewState
import com.moyasar.android.sdk.stcpay.presentation.model.formatter.SaudiPhoneNumberFormatter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class PaymentSheetViewModel(
    application: Application,
    private val paymentRequest: PaymentRequest,
    private val callback: (PaymentResult) -> Unit,
    private val createPaymentUseCase: CreatePaymentUseCase,
    private val createTokenUseCase: CreateTokenUseCase,
    private val validateSTCPayOTPUseCase: ValidateSTCPayOTPUseCase,
) : AndroidViewModel(application) {

    val inputFieldsValidatorLiveData: MutableLiveData<InputFieldsUIModel> =
        MutableLiveData<InputFieldsUIModel>()
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

    private val cleanCardNumber: String
        get() = inputFieldsValidatorLiveData.value?.cardNumber?.replace(" ", "").orEmpty()

    private val cleanMobileNumber: String
        get() = inputFieldsValidatorLiveData.value?.stcPayUIModel?.mobileNumber?.replace(" ", "")
            .orEmpty()

    private val expiryMonth: String
        get() = parseExpiry(inputFieldsValidatorLiveData.value?.expiryDate ?: "")?.month.toString()

    private val expiryYear: String
        get() = parseExpiry(inputFieldsValidatorLiveData.value?.expiryDate ?: "")?.year.toString()

    // Done logic like this to replicate iOS SDK's behavior
    val amountLabel: String
        get() = getFormattedAmount(paymentRequest)

    init {
        inputFieldsValidatorLiveData.value = InputFieldsUIModel()
    }

    private fun notifyPaymentResult(paymentResult: PaymentResult) = callback(paymentResult)

    /*************************
     * Perform Create payment Request After submit button clicked and createSaveOnlyToken = false
     * or After STC Pay Button Clicked
     ************************/
    internal fun createPayment(
        request: PaymentRequest = paymentRequest.copy(
            callbackUrl = PaymentAuthFragment.RETURN_URL,
            source = CardPaymentSource(
                name = inputFieldsValidatorLiveData.value?.name.orEmpty(),
                number = cleanCardNumber,
                month = expiryMonth,
                year = expiryYear,
                cvc = inputFieldsValidatorLiveData.value?.cvc.orEmpty(),
                manual = if (paymentRequest.manual) "true" else "false",
                saveCard = if (paymentRequest.saveCard) "true" else "false",
            ),
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
            name = inputFieldsValidatorLiveData.value?.name.orEmpty(),
            number = cleanCardNumber,
            cvc = inputFieldsValidatorLiveData.value?.cvc.orEmpty(),
            month = expiryMonth,
            year = expiryYear,
            saveOnly = true,
            callbackUrl = PaymentAuthFragment.RETURN_URL
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

    fun validateField(
        fieldType: FieldValidation,
        value: String,
        cardNumber: String,
        hasFocus: Boolean
    ) {

        when (fieldType) {
            FieldValidation.Name -> {
                when (hasFocus) {
                    true -> inputFieldsValidatorLiveData.value =
                        inputFieldsValidatorLiveData.value?.copy(
                            errorMessage = inputFieldsValidatorLiveData.value?.errorMessage?.copy(
                                nameErrorMsg = ""
                            ),
                        )

                    false -> getNameValidationRules().isValidName(value, inputFieldsValidatorLiveData)
                }
            }

            FieldValidation.Number -> {
                when (hasFocus) {
                    true -> {
                        inputFieldsValidatorLiveData.value =
                            inputFieldsValidatorLiveData.value?.copy(
                                errorMessage = inputFieldsValidatorLiveData.value?.errorMessage?.copy(
                                    numberErrorMsg = ""
                                )
                            )
                    }

                    false -> getNumberValidationRules().isValidNumber(value, inputFieldsValidatorLiveData)
                }
            }

            FieldValidation.Cvc -> {
                when (hasFocus ) {
                    true -> {
                        inputFieldsValidatorLiveData.value =
                            inputFieldsValidatorLiveData.value?.copy(
                                errorMessage = inputFieldsValidatorLiveData.value?.errorMessage?.copy(
                                    cvcErrorMsg = ""
                                )
                            )
                    }

                    false -> getCvcValidationRules(cardNumber).isValidCvc(value, inputFieldsValidatorLiveData)
                }
            }

            FieldValidation.Expiry -> {
                when (hasFocus) {
                    true -> {
                        inputFieldsValidatorLiveData.value =
                            inputFieldsValidatorLiveData.value?.copy(
                                errorMessage = inputFieldsValidatorLiveData.value?.errorMessage?.copy(
                                    expiryDateErrorMsg = ""
                                )
                            )
                    }

                    false -> getExpiryDateValidationRules().isValidExpiryDate(value, inputFieldsValidatorLiveData)
                }
            }
        }
    }





     fun creditCardNameChanged(name: String) {
        inputFieldsValidatorLiveData.value = inputFieldsValidatorLiveData.value?.copy(name = name)
        validateForm(inputFieldsValidatorLiveData.value)
    }


    private fun validateSTCMobile(textEdit: String) {
        inputFieldsValidatorLiveData.value =
            inputFieldsValidatorLiveData.value?.copy(
                stcPayUIModel = inputFieldsValidatorLiveData.value?.stcPayUIModel?.copy(
                    mobileNumber = textEdit,
                    isMobileValid = getPhoneNumberValidationRules().all {
                        it.predicate.invoke(
                            textEdit
                        ).not()
                    },
                    mobileNumberErrorMsg = getPhoneNumberValidationRules().firstOrNull {
                        it.predicate.invoke(
                            textEdit
                        )
                    }?.error.orEmpty()
                ) ?: STCPayUIModel(
                    mobileNumber = textEdit,
                    isMobileValid = getPhoneNumberValidationRules().all {
                        it.predicate.invoke(
                            textEdit
                        ).not()
                    },
                    mobileNumberErrorMsg = getPhoneNumberValidationRules().firstOrNull {
                        it.predicate.invoke(
                            textEdit
                        )
                    }?.error.orEmpty()
                ),
            )


    }

    private fun validateForm(inputFieldsUIModel: InputFieldsUIModel?) {
        inputFieldsValidatorLiveData.value = inputFieldsValidatorLiveData.value?.copy(
            isFormValid =
            getNameValidationRules().isValidName(inputFieldsUIModel?.name.orEmpty(), inputFieldsValidatorLiveData,false)
                && getNumberValidationRules().isValidNumber(
                inputFieldsUIModel?.cardNumber.orEmpty(),
                inputFieldsValidatorLiveData,
                false
            )
                && getCvcValidationRules(inputFieldsUIModel?.cardNumber.orEmpty()).isValidCvc(
                inputFieldsUIModel?.cvc.orEmpty(),
                inputFieldsValidatorLiveData,
                false
            )
                && getExpiryDateValidationRules().isValidExpiryDate(
                inputFieldsUIModel?.expiryDate.orEmpty(),
                inputFieldsValidatorLiveData,
                false
            )

        )
    }

    fun creditCardNumberChanged(textEdit: Editable?, onUpdateText: (String) -> Unit) {
        if (textEdit==null) return
        inputFieldsValidatorLiveData.value =
            inputFieldsValidatorLiveData.value?.copy(cardNumber = textEdit.toString())
        if (ccOnChangeLocked) {
            return
        }
        ccOnChangeLocked = true
        val formatted = CreditCardFormatter.formatCardNumber(textEdit.toString())
        onUpdateText(formatted)
        validateForm(inputFieldsValidatorLiveData.value)
        ccOnChangeLocked = false
    }

    fun mobileNumberChanged(textEdit: Editable?, onUpdateText: (String) -> Unit) {
        if (textEdit == null) return
        if (mobileNumberOnChangeLocked) {
            return
        }
        mobileNumberOnChangeLocked = true
        val formatted = SaudiPhoneNumberFormatter.formatPhoneNumber(textEdit.toString())
        onUpdateText(formatted)
        validateSTCMobile(formatted.replace(" ", ""))
        mobileNumberOnChangeLocked = false

    }


    fun creditCardExpiryChanged(textEdit: Editable?, onUpdateText: (String) -> Unit) {
        if (textEdit == null) return
        inputFieldsValidatorLiveData.value =
            inputFieldsValidatorLiveData.value?.copy(expiryDate = textEdit.toString())
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

        onUpdateText(formatted.toString())
        validateForm(inputFieldsValidatorLiveData.value)
        ccExpiryOnChangeLocked = false
    }

    fun creditCardCvcChanged(textEdit: Editable?) {
        inputFieldsValidatorLiveData.value =
            inputFieldsValidatorLiveData.value?.copy(cvc = textEdit.toString())
        validateForm(inputFieldsValidatorLiveData.value)
    }

     fun submit() {
        if (inputFieldsValidatorLiveData.value?.isFormValid == false) {
            return
        }

        if (_creditCardStatus.value != PaymentStatusViewState.Reset) {
            return
        }

        _creditCardStatus.value = PaymentStatusViewState.SubmittingPayment

        if (paymentRequest.createSaveOnlyToken) {
            createSaveOnlyToken()
        } else {
            createPayment()
        }
    }

     fun submitSTC() {
        if (inputFieldsValidatorLiveData.value?.stcPayUIModel?.isMobileValid == false) {
            return
        }

        _stcPayStatus.value = STCPayViewState.SubmittingSTCPayMobileNumber
        createPayment(
            request = paymentRequest.copy(
                callbackUrl = PaymentAuthFragment.RETURN_URL,
                source = STCPayPaymentSource(
                    mobile = cleanMobileNumber,
                )
            )
        )

    }

    fun submitSTCPayOTP(
        transactionURL: String, otp: String, mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) {
        _stcPayStatus.value = STCPayViewState.SubmittingSTCPayOTP
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

    fun stcPayOTPChanged(textEdit: Editable?) {
        if (textEdit == null) return
        inputFieldsValidatorLiveData.value =
            inputFieldsValidatorLiveData.value?.copy(
                stcPayUIModel = inputFieldsValidatorLiveData.value?.stcPayUIModel?.copy(
                    otp = textEdit.toString(),
                    isOTPValid = getOTPValidationRules().all {
                        it.predicate.invoke(textEdit.toString()).not()
                    },
                    otpErrorMsg = getOTPValidationRules().firstOrNull { it.predicate.invoke(textEdit.toString()) }?.error.orEmpty()
                ) ?: STCPayUIModel(
                    otp = textEdit.toString(),
                    isOTPValid = getOTPValidationRules().all {
                        it.predicate.invoke(textEdit.toString()).not()
                    },
                    otpErrorMsg = getOTPValidationRules().firstOrNull { it.predicate.invoke(textEdit.toString()) }?.error.orEmpty()
                ),
            )
    }
}
