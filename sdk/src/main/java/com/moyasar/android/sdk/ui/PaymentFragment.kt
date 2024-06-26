package com.moyasar.android.sdk.ui

import android.app.Application
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.moyasar.android.sdk.InvalidConfigException
import com.moyasar.android.sdk.PaymentConfig
import com.moyasar.android.sdk.PaymentResult
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.data.PaymentSheetViewModel
import com.moyasar.android.sdk.data.SharedPaymentViewModelHolder
import com.moyasar.android.sdk.databinding.FragmentPaymentBinding
import com.moyasar.android.sdk.extensions.afterTextChanged
import com.moyasar.android.sdk.extensions.shouldDisableButton
import com.moyasar.android.sdk.extensions.showCcNumberIconsWhenEmpty

class PaymentFragment : Fragment() {

  private val viewModel: PaymentSheetViewModel = SharedPaymentViewModelHolder.sharedViewModel

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

      SharedPaymentViewModelHolder.sharedViewModel =
        PaymentSheetViewModel(application, config, callback)
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
    binding.payButton.text = getString(R.string.payBtnLabel).plus(' ').plus(viewModel.amountLabel)
  }

  private fun setupObservers() {
    viewModel.status.observe(viewLifecycleOwner, ::handleOnStatusChanged)
    viewModel.isFormValid.observe(viewLifecycleOwner, ::handleFormValidationState)
    viewModel.number.observe(viewLifecycleOwner, ::handleCardNumberValueUpdated)
    /*  Handle Form Validation Errors  */
    viewModel.nameValidator.error.observe(viewLifecycleOwner, ::showInvalidNameErrorMsg)
    viewModel.numberValidator.error.observe(viewLifecycleOwner, ::showInvalidCardNumberErrorMsg)
    viewModel.expiryValidator.error.observe(viewLifecycleOwner, ::showInvalidExpiryErrorMsg)
    viewModel.cvcValidator.error.observe(viewLifecycleOwner, ::showInvalidCVVErrorMsg)
  }

  private fun handleOnStatusChanged(status: PaymentSheetViewModel.Status?) {
    parentActivity.runOnUiThread {
      when (status) {
        is PaymentSheetViewModel.Status.PaymentAuth3dSecure -> {
          hideLoading()
          hideScreenViews()
          childFragmentManager.beginTransaction().apply {
            replace(R.id.payment_fragment_container, PaymentAuthFragment())
            commit()
          }
        }

        is PaymentSheetViewModel.Status.Reset -> {
          hideLoading()
          showScreenViews()
        }

        is PaymentSheetViewModel.Status.SubmittingPayment -> {
          showLoading()
          hideScreenViews()
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
    binding.circularProgressIndicator.visibility = VISIBLE
  }

  private fun hideLoading() {
    binding.circularProgressIndicator.visibility = INVISIBLE
  }

  private fun showScreenViews() = with(binding){
    payButton.visibility = VISIBLE
    nameOnCardInput.visibility = VISIBLE
    cardNumberInput.visibility = VISIBLE
    cardExpiryDateInput.visibility = VISIBLE
    cardSecurityCodeInput.visibility = VISIBLE
  }

  private fun hideScreenViews() = with(binding) {
      payButton.visibility = INVISIBLE
      nameOnCardInput.visibility = INVISIBLE
      cardNumberInput.visibility = INVISIBLE
      cardExpiryDateInput.visibility = INVISIBLE
      cardSecurityCodeInput.visibility = INVISIBLE
    }



  private fun FragmentPaymentBinding.setupListeners() {
    etNameOnCardInput.afterTextChanged { text ->
      viewModel.name.value = text?.toString()
      viewModel.creditCardNameChanged()
    }

    etCardNumberInput.afterTextChanged { text ->
      viewModel.number.value = text?.toString()
      text?.let { viewModel.creditCardNumberChanged(it) }
    }

    etCardExpiryDateInput.afterTextChanged { text ->
      viewModel.expiry.value = text?.toString()
      text?.let { viewModel.creditCardExpiryChanged(it) }
    }

    etCardSecurityCodeInput.afterTextChanged { text ->
      viewModel.cvc.value = text?.toString()
      viewModel.creditCardCvcChanged()
    }

    etNameOnCardInput.setOnFocusChangeListener { _, hf ->
      viewModel.validateField(PaymentSheetViewModel.FieldValidation.Name, hf)
    }
    etCardNumberInput.setOnFocusChangeListener { _, hf ->
      viewModel.validateField(PaymentSheetViewModel.FieldValidation.Number, hf)
    }
    etCardExpiryDateInput.setOnFocusChangeListener { _, hf ->
      viewModel.validateField(PaymentSheetViewModel.FieldValidation.Expiry, hf)
    }
    etCardSecurityCodeInput.setOnFocusChangeListener { _, hf ->
      viewModel.validateField(PaymentSheetViewModel.FieldValidation.Cvc, hf)
    }

    payButton.setOnClickListener {
      viewModel.submit()
    }
  }


}