package com.moyasar.android.sdk.creditcard.presentation.view.fragments

import android.app.Application
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.core.customviews.button.MoyasarButtonType
import com.moyasar.android.sdk.core.exceptions.InvalidConfigException
import com.moyasar.android.sdk.core.extensions.afterTextChanged
import com.moyasar.android.sdk.core.extensions.hide
import com.moyasar.android.sdk.core.extensions.shouldDisableButton
import com.moyasar.android.sdk.core.extensions.show
import com.moyasar.android.sdk.core.extensions.showCcNumberIconsWhenEmpty
import com.moyasar.android.sdk.databinding.FragmentPaymentBinding
import com.moyasar.android.sdk.core.domain.entities.PaymentResult
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer
import com.moyasar.android.sdk.creditcard.presentation.di.MoyasarAppContainer.viewModel
import com.moyasar.android.sdk.creditcard.presentation.model.FieldValidation
import com.moyasar.android.sdk.creditcard.presentation.model.PaymentConfig
import com.moyasar.android.sdk.creditcard.presentation.model.PaymentStatusViewState

class PaymentFragment : Fragment() {

  private lateinit var parentActivity: FragmentActivity
  private lateinit var binding: FragmentPaymentBinding

  companion object {
    fun newInstance(
      application: Application,
      config: PaymentConfig,
      callback: (PaymentResult) -> Unit,
    ): PaymentFragment {
      val configError = config.validate()
      if (configError.any()) {
        throw InvalidConfigException(configError)
      }
      MoyasarAppContainer.initialize(application, config, callback)
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
    binding.payButton.setButtonType(MoyasarButtonType.PAY)
  }

  private fun setupObservers() {
    viewModel.status.observe(viewLifecycleOwner, ::handleOnStatusChanged)
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
    binding.etCardNumberInput.showCcNumberIconsWhenEmpty(number.orEmpty())
  }

  private fun showInvalidCVVErrorMsg(errorMsg: String?) {
    binding.cardSecurityCodeInput.error = errorMsg
  }

  private fun showInvalidExpiryErrorMsg(errorMsg: String?) {
    binding.cardExpiryDateInput.error = errorMsg
  }

  private fun showInvalidCardNumberErrorMsg(errorMsg: String?) {
    binding.cardNumberInput.error = errorMsg
  }

  private fun showInvalidNameErrorMsg(errorMsg: String?) {
    binding.nameOnCardInput.error = errorMsg
  }


  private fun showLoading() {
    binding.circularProgressIndicator.show()
  }

  private fun hideLoading() {
    binding.circularProgressIndicator.hide()
  }

  private fun showScreenViews() = with(binding) {
    payButton.show()
    nameOnCardInput.show()
    cardNumberInput.show()
    cardExpiryDateInput.show()
    cardSecurityCodeInput.show()
  }

  private fun enableScreenViews() = with(binding) {
    payButton.isEnabled = true
    nameOnCardInput.isEnabled = true
    cardNumberInput.isEnabled = true
    cardExpiryDateInput.isEnabled = true
    cardSecurityCodeInput.isEnabled = true
  }

  private fun disableScreenViews() = with(binding) {
    payButton.isEnabled = false
    nameOnCardInput.isEnabled = false
    cardNumberInput.isEnabled = false
    cardExpiryDateInput.isEnabled = false
    cardSecurityCodeInput.isEnabled = false
  }

  private fun hideScreenViews() = with(binding) {
    payButton.hide()
    nameOnCardInput.hide()
    cardNumberInput.hide()
    cardExpiryDateInput.hide()
    cardSecurityCodeInput.hide()
  }


  private fun FragmentPaymentBinding.setupListeners() {
    etNameOnCardInput.afterTextChanged { text ->
      viewModel.formValidator.name.value = text?.toString()
      viewModel.creditCardNameChanged()
    }

    etCardNumberInput.afterTextChanged { text ->
      viewModel.formValidator.number.value = text?.toString()
      text?.let { viewModel.creditCardNumberChanged(it) }
    }

    etCardExpiryDateInput.afterTextChanged { text ->
      viewModel.formValidator.expiry.value = text?.toString()
      text?.let { viewModel.creditCardExpiryChanged(it) }
    }

    etCardSecurityCodeInput.afterTextChanged { text ->
      viewModel.formValidator.cvc.value = text?.toString()
      viewModel.creditCardCvcChanged()
    }

    etNameOnCardInput.setOnFocusChangeListener { _, hf ->
      viewModel.validateField(FieldValidation.Name, hf)
    }
    etCardNumberInput.setOnFocusChangeListener { _, hf ->
      viewModel.validateField(FieldValidation.Number, hf)
    }
    etCardExpiryDateInput.setOnFocusChangeListener { _, hf ->
      viewModel.validateField(FieldValidation.Expiry, hf)
    }
    etCardSecurityCodeInput.setOnFocusChangeListener { _, hf ->
      viewModel.validateField(FieldValidation.Cvc, hf)
    }

    payButton.setOnClickListener {
      viewModel.submit()
    }
  }

}

