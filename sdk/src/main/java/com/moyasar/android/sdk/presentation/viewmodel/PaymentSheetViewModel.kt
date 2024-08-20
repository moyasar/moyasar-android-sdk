package com.moyasar.android.sdk.presentation.viewmodel

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
import com.moyasar.android.sdk.data.models.CardPaymentSource
import com.moyasar.android.sdk.data.models.Payment
import com.moyasar.android.sdk.data.models.PaymentRequest
import com.moyasar.android.sdk.data.models.TokenRequest
import com.moyasar.android.sdk.domain.entities.PaymentResult
import com.moyasar.android.sdk.domain.usecases.CreatePaymentUseCase
import com.moyasar.android.sdk.domain.usecases.CreateTokenUseCase
import com.moyasar.android.sdk.presentation.model.AuthResultViewState
import com.moyasar.android.sdk.presentation.model.FieldValidation
import com.moyasar.android.sdk.presentation.model.PaymentConfig
import com.moyasar.android.sdk.presentation.model.PaymentStatusViewState
import com.moyasar.android.sdk.presentation.model.RequestResultViewState
import com.moyasar.android.sdk.presentation.view.fragments.PaymentAuthFragment
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class PaymentSheetViewModel(
  application: Application,
  private val paymentConfig: PaymentConfig,
  private val callback: (PaymentResult) -> Unit,
  internal val formValidator: FormValidator,
  private val createPaymentUseCase: CreatePaymentUseCase,
  private val createTokenUseCase: CreateTokenUseCase,
) : AndroidViewModel(application) {


  private var ccOnChangeLocked = false
  private var ccExpiryOnChangeLocked = false

  private val _status =
    MutableLiveData<PaymentStatusViewState>().default(PaymentStatusViewState.Reset)
  private val _payment = MutableLiveData<Payment?>()

  internal val payment: LiveData<Payment?> = _payment
  val status: LiveData<PaymentStatusViewState> = _status
  val isFormValid: LiveData<Boolean> = formValidator._isFormValid.distinctUntilChanged()


  private val cleanCardNumber: String
    get() = formValidator.number.value!!.replace(" ", "")

  private val expiryMonth: String
    get() = parseExpiry(formValidator.expiry.value ?: "")?.month.toString()

  private val expiryYear: String
    get() = parseExpiry(formValidator.expiry.value ?: "")?.year.toString()

  // Done logic like this to replicate iOS SDK's behavior
  val amountLabel: String
    get() = getFormattedAmount(paymentConfig)

   fun notifyPaymentResult(paymentResult: PaymentResult) = callback(paymentResult)

  /*************************
   * Perform Create payment Request After submit button clicked and createSaveOnlyToken = false
   ************************/
  internal fun createPayment(
    request: PaymentRequest = PaymentRequest(
      paymentConfig.amount,
      paymentConfig.currency,
      paymentConfig.description,
      PaymentAuthFragment.RETURN_URL,
      CardPaymentSource(
        formValidator.name.value!!,
        cleanCardNumber,
        expiryMonth,
        expiryYear,
        formValidator.cvc.value!!,
        if (paymentConfig.manual) "true" else "false",
        if (paymentConfig.saveCard) "true" else "false",
      ),
      paymentConfig.metadata ?: HashMap()
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
              _status.value =
                PaymentStatusViewState.PaymentAuth3dSecure(result.data.getCardTransactionUrl())
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

    if (_status.value != PaymentStatusViewState.Reset) {
      return
    }

    _status.value = PaymentStatusViewState.SubmittingPayment

    if (paymentConfig.createSaveOnlyToken) {
      createSaveOnlyToken()
    } else {
      createPayment()
    }
  }
}
