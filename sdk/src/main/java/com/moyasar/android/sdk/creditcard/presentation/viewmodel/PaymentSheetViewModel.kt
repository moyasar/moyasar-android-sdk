package com.moyasar.android.sdk.creditcard.presentation.viewmodel

import android.app.Application
import android.text.Editable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.core.data.response.PaymentResponse
import com.moyasar.android.sdk.core.domain.entities.PaymentResult
import com.moyasar.android.sdk.core.exceptions.PaymentSheetException
import com.moyasar.android.sdk.core.extensions.default
import com.moyasar.android.sdk.core.extensions.scope
import com.moyasar.android.sdk.core.util.CreditCardFormatter
import com.moyasar.android.sdk.core.util.LiveDataValidator.ValidationRule
import com.moyasar.android.sdk.core.util.cleanSpaces
import com.moyasar.android.sdk.core.util.getFormattedAmount
import com.moyasar.android.sdk.core.util.isValidLuhnNumber
import com.moyasar.android.sdk.core.util.parseExpiry
import com.moyasar.android.sdk.creditcard.data.models.CreditCardNetwork
import com.moyasar.android.sdk.creditcard.data.models.getNetwork
import com.moyasar.android.sdk.creditcard.data.models.isCreditAllowed
import com.moyasar.android.sdk.creditcard.data.models.request.PaymentRequest
import com.moyasar.android.sdk.creditcard.data.models.request.TokenRequest
import com.moyasar.android.sdk.creditcard.data.models.sources.CardPaymentSource
import com.moyasar.android.sdk.creditcard.domain.usecases.CreatePaymentUseCase
import com.moyasar.android.sdk.creditcard.domain.usecases.CreateTokenUseCase
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer.application
import com.moyasar.android.sdk.creditcard.presentation.model.AuthResultViewState
import com.moyasar.android.sdk.creditcard.presentation.model.FieldValidation
import com.moyasar.android.sdk.creditcard.presentation.model.FormErrorMessage
import com.moyasar.android.sdk.creditcard.presentation.model.InputFieldsUIModel
import com.moyasar.android.sdk.creditcard.presentation.model.PaymentStatusViewState
import com.moyasar.android.sdk.creditcard.presentation.model.RequestResultViewState
import com.moyasar.android.sdk.creditcard.presentation.model.STCPayUIModel
import com.moyasar.android.sdk.creditcard.presentation.view.fragments.PaymentAuthFragment
import com.moyasar.android.sdk.stcpay.data.models.sources.STCPayPaymentSource
import com.moyasar.android.sdk.stcpay.domain.usecases.ValidateSTCPayOTPUseCase
import com.moyasar.android.sdk.stcpay.presentation.model.STCPayViewState
import com.moyasar.android.sdk.stcpay.presentation.model.formatter.SaudiPhoneNumberFormatter
import com.moyasar.android.sdk.stcpay.presentation.model.validator.STCPayOTPValidator
import com.moyasar.android.sdk.stcpay.presentation.model.validator.SaudiPhoneNumberValidator
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

    private var isNameVisitedBefore = false
    private var isNumberVisitedBefore = false
    private var isExpiryVisitedBefore = false
    private var isCvvVisitedBefore = false
    fun validateField(
        fieldType: FieldValidation,
        value: String,
       /// name: String,
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

                    false -> getNameValidationRules().isValidName(value)
                }
            }

            FieldValidation.Number -> {
                when (hasFocus) {
                    true -> {
                      ///  getNameValidationRules().isValidName(name)
                        inputFieldsValidatorLiveData.value =
                            inputFieldsValidatorLiveData.value?.copy(
                                errorMessage = inputFieldsValidatorLiveData.value?.errorMessage?.copy(
                                    numberErrorMsg = ""
                                )
                            )
                    }

                    false -> getNumberValidationRules().isValidNumber(value)
                }
            }

            FieldValidation.Cvc -> {
                when (hasFocus ) {
                    true -> {
                       /// getNameValidationRules().isValidName(name)
                        inputFieldsValidatorLiveData.value =
                            inputFieldsValidatorLiveData.value?.copy(
                                errorMessage = inputFieldsValidatorLiveData.value?.errorMessage?.copy(
                                    cvcErrorMsg = ""
                                )
                            )
                    }

                    false -> getCvcValidationRules(cardNumber).isValidCvc(value)
                }
            }

            FieldValidation.Expiry -> {
                when (hasFocus) {
                    true -> {
                     ///   getNameValidationRules().isValidName(name)
                        inputFieldsValidatorLiveData.value =
                            inputFieldsValidatorLiveData.value?.copy(
                                errorMessage = inputFieldsValidatorLiveData.value?.errorMessage?.copy(
                                    expiryDateErrorMsg = ""
                                )
                            )
                    }

                    false -> getExpiryDateValidationRules().isValidExpiryDate(value)
                }
            }
        }
    }

    private fun getNameValidationRules(): MutableList<ValidationRule> {
        val rules = mutableListOf<ValidationRule>()
        val latinRegex = Regex("^[a-zA-Z\\-\\s]+\$")
        val nameRegex = Regex("^[a-zA-Z\\-]+\\s+?([a-zA-Z\\-]+\\s?)+\$")

        rules.add(
            ValidationRule(
                { it.isNullOrBlank() },
                application.getString(R.string.name_is_required)
            )
        )
        rules.add(ValidationRule({
            !latinRegex.matches(
                it ?: ""
            )
        }, application.getString(R.string.only_english_alpha)))

        rules.add(ValidationRule({
            !nameRegex.matches(
                it ?: ""
            )
        }, application.getString(R.string.both_names_required)))
        return rules
    }

    private fun getNumberValidationRules(): MutableList<ValidationRule> {
        val rules = mutableListOf<ValidationRule>()
        rules.add(
            ValidationRule(
                { it.isNullOrBlank() },
                application.getString(R.string.card_number_required)
            )
        )
        rules.add(ValidationRule({
            !isValidLuhnNumber(
                it ?: ""
            ) || it?.cleanSpaces().orEmpty().length < 15
        }, application.getString(R.string.invalid_card_number)))
        rules.add(ValidationRule({
            getNetwork(
                number = it ?: ""
            ) == CreditCardNetwork.Unknown || !isCreditAllowed(
                number = it ?: "",
                allowedNetwork = MoyasarAppContainer.paymentRequest.allowedNetworks
            )
        }, application.getString(R.string.unsupported_network)))

        return rules
    }

    private fun getCvcValidationRules(number: String): MutableList<ValidationRule> {
        val rules = mutableListOf<ValidationRule>()
        rules.add(
            ValidationRule(
                { it.isNullOrBlank() },
                application.getString(R.string.cvc_required)
            )
        )
        rules.add(ValidationRule({
            when (getNetwork(
                number = number
            )) {
                CreditCardNetwork.Amex -> (it?.length ?: 0) < 4
                else -> (it?.length ?: 0) < 3
            }
        }, application.getString(R.string.invalid_cvc)))
        return rules
    }

    private fun getExpiryDateValidationRules(): MutableList<ValidationRule> {
        val rules = mutableListOf<ValidationRule>()
        rules.add(
            ValidationRule(
                { it.isNullOrBlank() },
                application.getString(R.string.expiry_is_required)
            )
        )
        rules.add(ValidationRule({
            parseExpiry(it ?: "")?.isInvalid() ?: true
        }, application.getString(R.string.invalid_expiry)))
        rules.add(ValidationRule({
            parseExpiry(it ?: "")?.expired() ?: false
        }, application.getString(R.string.expired_card)))
        return rules
    }

    private fun getPhoneNumberValidationRules(): MutableList<ValidationRule> {
        val rules = mutableListOf<ValidationRule>()
        rules.add(
            ValidationRule(
                { it.isNullOrBlank() },
                application.getString(R.string.mobile_number_is_required)
            )
        )
        rules.add(
            ValidationRule(
                {
                    !SaudiPhoneNumberValidator.isValidSaudiPhoneNumber(
                        it.orEmpty()
                    )
                },
                application.getString(R.string.invalid_mobile_number)
            )
        )
        return rules
    }

    private fun getOTPValidationRules(): MutableList<ValidationRule> {
        val rules = mutableListOf<ValidationRule>()
        rules.add(
            ValidationRule(
                {
                    !STCPayOTPValidator.isValidOtp(
                        it ?: ""
                    )
                },
                application.getString(R.string.invalid_stc_pay_otp)
            )
        )

        return rules
    }

    private fun List<ValidationRule>.isValidName(
        value: String,
        isShowError: Boolean = true,
    ): Boolean {
        val rules = this
        for (rule in rules) {
            if (rule.predicate(value)) {
                Log.e("BBB",""+value)
                if (isShowError)
                    inputFieldsValidatorLiveData.value =
                        inputFieldsValidatorLiveData.value?.copy(
                            errorMessage = inputFieldsValidatorLiveData.value?.errorMessage?.copy(
                                nameErrorMsg = rule.error
                            ) ?: FormErrorMessage(nameErrorMsg = rule.error)
                        )
                return false
            }
        }

        return true
    }


    private fun List<ValidationRule>.isValidNumber(
        value: String,
        isShowError: Boolean = true,
    ): Boolean {
        val rules = this
        for (rule in rules) {
            if (rule.predicate(value)) {
                if (isShowError)
                    inputFieldsValidatorLiveData.value =
                        inputFieldsValidatorLiveData.value?.copy(
                            errorMessage = inputFieldsValidatorLiveData.value?.errorMessage?.copy(
                                numberErrorMsg = rule.error
                            ) ?: FormErrorMessage(numberErrorMsg = rule.error)
                        )
                return false
            }
        }

        return true
    }

    private fun List<ValidationRule>.isValidCvc(
        value: String,
        isShowError: Boolean = true,
    ): Boolean {
        val rules = this
        for (rule in rules) {
            if (rule.predicate(value)) {
                if (isShowError)
                    inputFieldsValidatorLiveData.value =
                        inputFieldsValidatorLiveData.value?.copy(
                            errorMessage = inputFieldsValidatorLiveData.value?.errorMessage?.copy(
                                cvcErrorMsg = rule.error
                            ) ?: FormErrorMessage(cvcErrorMsg = rule.error)
                        )
                return false
            }
        }

        return true
    }

    private fun List<ValidationRule>.isValidExpiryDate(
        value: String,
        isShowError: Boolean = true,
    ): Boolean {
        val rules = this
        for (rule in rules) {
            if (rule.predicate(value)) {
                if (isShowError)
                    inputFieldsValidatorLiveData.value =
                        inputFieldsValidatorLiveData.value?.copy(
                            errorMessage = inputFieldsValidatorLiveData.value?.errorMessage?.copy(
                                expiryDateErrorMsg = rule.error
                            ) ?: FormErrorMessage(expiryDateErrorMsg = rule.error)
                        )
                return false
            }
        }

        return true
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
                            textEdit.toString()
                        ).not()
                    },
                    mobileNumberErrorMsg = getPhoneNumberValidationRules().firstOrNull {
                        it.predicate.invoke(
                            textEdit.toString()
                        )
                    }?.error.orEmpty()
                ) ?: STCPayUIModel(
                    mobileNumber = textEdit,
                    isMobileValid = getPhoneNumberValidationRules().all {
                        it.predicate.invoke(
                            textEdit.toString()
                        ).not()
                    },
                    mobileNumberErrorMsg = getPhoneNumberValidationRules().firstOrNull {
                        it.predicate.invoke(
                            textEdit.toString()
                        )
                    }?.error.orEmpty()
                ),
            )


    }

    private fun validateForm(inputFieldsUIModel: InputFieldsUIModel?) {
        inputFieldsValidatorLiveData.value = inputFieldsValidatorLiveData.value?.copy(
            isFormValid =
            getNameValidationRules().isValidName(inputFieldsUIModel?.name.orEmpty(), false)
                && getNumberValidationRules().isValidNumber(
                inputFieldsUIModel?.cardNumber.orEmpty(),
                false
            )
                && getCvcValidationRules(inputFieldsUIModel?.cardNumber.orEmpty()).isValidCvc(
                inputFieldsUIModel?.cvc.orEmpty(),
                false
            )
                && getExpiryDateValidationRules().isValidExpiryDate(
                inputFieldsUIModel?.expiryDate.orEmpty(),
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
     ///   textEdit.replace(0, textEdit.length, formatted)
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
