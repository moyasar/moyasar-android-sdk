package com.moyasar.android.sdk.creditcard.presentation.view.fragments

import android.app.Application
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.core.domain.entities.PaymentResult
import com.moyasar.android.sdk.core.exceptions.InvalidConfigException
import com.moyasar.android.sdk.core.extensions.afterTextChanged
import com.moyasar.android.sdk.core.extensions.hide
import com.moyasar.android.sdk.core.extensions.shouldDisableButton
import com.moyasar.android.sdk.core.extensions.show
import com.moyasar.android.sdk.creditcard.data.models.request.PaymentRequest
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer.paymentRequest
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer.viewModel
import com.moyasar.android.sdk.creditcard.presentation.model.FieldValidation
import com.moyasar.android.sdk.creditcard.presentation.model.InputCreditCardUIModel
import com.moyasar.android.sdk.creditcard.presentation.model.PaymentStatusViewState
import com.moyasar.android.sdk.creditcard.presentation.model.showAllowedCreditCardsInEditText
import com.moyasar.android.sdk.databinding.FragmentPaymentBinding

class PaymentFragment : Fragment() {

  private lateinit var parentActivity: FragmentActivity
  private lateinit var binding: FragmentPaymentBinding

  companion object {
    fun newInstance(
      application: Application,
      paymentRequest: PaymentRequest,
      callback: (PaymentResult) -> Unit,
    ): PaymentFragment {
      val configError = paymentRequest.validate()
      if (configError.any()) {
        throw InvalidConfigException(configError)
      }
      MoyasarAppContainer.initialize(application, paymentRequest, callback)
      return PaymentFragment()
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    super.onCreateView(inflater, container, savedInstanceState)
    parentActivity = requireActivity()
    binding = FragmentPaymentBinding.inflate(inflater, container, false)
    initView()
    setupObservers()
    binding.setupListeners()
    return binding.root
  }

  private fun initView() {
    // holder
    binding.etCardHolder.setLabelText(getString(R.string.name_on_card_label))
    binding.etCardHolder.setHintText(getString(R.string.name_on_card_label))
    binding.etCardHolder.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
    // card
      binding.viewCard.setLabelText(getString(R.string.card_label))
    val inputCreditCardUIModel = InputCreditCardUIModel(
      numberHint = getString(R.string.credit_card_label),
      expiryDateHint = getString(R.string.expiration_date_label),
      cvcHint = getString(R.string.security_code_label),
      numberType = InputType.TYPE_CLASS_NUMBER,
      expiryDateType =  InputType.TYPE_CLASS_NUMBER,
      cvcType =  InputType.TYPE_CLASS_NUMBER,

    )
    binding.viewCard.setHintText(inputCreditCardUIModel)
    binding.viewCard.setInputType(inputCreditCardUIModel)


    binding.payButton.setButtonType(paymentRequest.buttonType)
  }

  private fun setupObservers() {
    viewModel.creditCardStatus.observe(viewLifecycleOwner, ::handleOnStatusChanged)
    viewModel.isFormValid.observe(viewLifecycleOwner, ::handleFormValidationState)
    viewModel.formValidator.number.observe(viewLifecycleOwner, ::handleCardNumberValueUpdated)
    /*  Handle Form Validation Errors  */
    viewModel.formValidator.nameValidator.error.observe(viewLifecycleOwner, ::showInvalidNameErrorMsg)
    viewModel.formValidator.numberValidator.error.observe(viewLifecycleOwner, ::showInvalidCardNumberErrorMsg)
    viewModel.formValidator.expiryValidator.error.observe(viewLifecycleOwner, ::showInvalidExpiryErrorMsg)
    viewModel.formValidator.cvcValidator.error.observe(viewLifecycleOwner, ::showInvalidCVVErrorMsg)
  }

  private fun handleOnStatusChanged(status: PaymentStatusViewState?) {
    parentActivity.runOnUiThread {
      when (status) {
        is PaymentStatusViewState.PaymentAuth3dSecure -> {
          hideLoading()
          hideScreenViews()
          disableScreenViews()
          childFragmentManager.beginTransaction().apply {
            replace(R.id.payment_fragment_container, PaymentAuthFragment())
            commit()
          }
        }

        is PaymentStatusViewState.Reset -> {
          hideLoading()
          showScreenViews()
          enableScreenViews()
        }

        is PaymentStatusViewState.SubmittingPayment -> {
          showLoading()
          hideScreenViews()
          disableScreenViews()
        }

        else -> Unit
      }
    }
  }

  private fun handleFormValidationState(isFormValid: Boolean?) {
    binding.payButton.shouldDisableButton(isFormValid ?: false)
  }

  private fun handleCardNumberValueUpdated(number: String?) {
    showAllowedCreditCardsInEditText(number.orEmpty(), paymentRequest.allowedNetworks, binding)
  }

  private fun showInvalidCVVErrorMsg(errorMsg: String?) {
    binding.viewCard.setError(errorMsg)
  }

  private fun showInvalidExpiryErrorMsg(errorMsg: String?) {
    binding.viewCard.setError(errorMsg)

  }

  private fun showInvalidCardNumberErrorMsg(errorMsg: String?) {
    binding.viewCard.setError(errorMsg)
  }

  private fun showInvalidNameErrorMsg(errorMsg: String?) {
    binding.etCardHolder.setError(errorMsg)
  }


  private fun showLoading() {
    binding.circularProgressIndicator.show()
  }

  private fun hideLoading() {
    binding.circularProgressIndicator.hide()
  }

  private fun showScreenViews() = with(binding) {
    payButton.show()
    etCardHolder.show()
    viewCard.show()
  }

  private fun enableScreenViews() = with(binding) {
    payButton.isEnabled = true
    etCardHolder.inputEditText.isEnabled = true
    viewCard.inputEditTextCardNumber.isEnabled = true
    viewCard.inputEditTextCardExpiryDate.isEnabled = true
    viewCard.inputEditTextCardCvc.isEnabled = true
  }

  private fun disableScreenViews() = with(binding) {
    payButton.isEnabled = false
    etCardHolder.inputEditText.isEnabled = false
    viewCard.inputEditTextCardNumber.isEnabled = false
    viewCard.inputEditTextCardExpiryDate.isEnabled = false
    viewCard.inputEditTextCardCvc.isEnabled = false
  }

  private fun hideScreenViews() = with(binding) {
    payButton.hide()
    etCardHolder.hide()
   viewCard.hide()
  }


  private fun FragmentPaymentBinding.setupListeners() {
    etCardHolder.inputEditText.afterTextChanged { text ->
      viewModel.formValidator.name.value = text?.toString()
      viewModel.creditCardNameChanged()
    }

    viewCard.inputEditTextCardNumber.afterTextChanged { text ->
      viewModel.formValidator.number.value = text?.toString()
      text?.let { viewModel.creditCardNumberChanged(it) }
    }

    viewCard.inputEditTextCardExpiryDate.afterTextChanged { text ->
      viewModel.formValidator.expiry.value = text?.toString()
      text?.let { viewModel.creditCardExpiryChanged(it) }
    }

    viewCard.inputEditTextCardCvc.afterTextChanged { text ->
      viewModel.formValidator.cvc.value = text?.toString()
      viewModel.creditCardCvcChanged()
    }

    etCardHolder.inputEditText.setOnFocusChangeListener { _, hf ->
      Log.e("inputEditText",""+hf)
      viewModel.validateField(FieldValidation.Name, hf)
    }
    viewCard.inputEditTextCardNumber.setOnFocusChangeListener { _, hf ->
      viewModel.validateField(FieldValidation.Number, hf)
    }
    viewCard.inputEditTextCardExpiryDate.setOnFocusChangeListener { _, hf ->
      viewModel.validateField(FieldValidation.Expiry, hf)
    }
    viewCard.inputEditTextCardCvc.setOnFocusChangeListener { _, hf ->
      viewModel.validateField(FieldValidation.Cvc, hf)
    }

    payButton.setOnClickListener {
      viewModel.submit()
    }
  }

}

