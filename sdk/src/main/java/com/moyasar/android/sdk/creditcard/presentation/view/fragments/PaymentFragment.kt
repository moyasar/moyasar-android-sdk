package com.moyasar.android.sdk.creditcard.presentation.view.fragments

import android.app.Application
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.moyasar.android.sdk.R
import com.moyasar.android.sdk.core.domain.entities.PaymentResult
import com.moyasar.android.sdk.core.exceptions.InvalidConfigException
import com.moyasar.android.sdk.core.extensions.afterTextChanged
import com.moyasar.android.sdk.core.extensions.gone
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
        binding.viewCard.labelTextView.text = (getString(R.string.card_label))
        val inputCreditCardUIModel = InputCreditCardUIModel(
            numberHint = getString(R.string.credit_card_label),
            expiryDateHint = getString(R.string.expiration_date_label),
            cvcHint = getString(R.string.security_code_label),
            numberType = InputType.TYPE_CLASS_NUMBER,
            expiryDateType = InputType.TYPE_CLASS_NUMBER,
            cvcType = InputType.TYPE_CLASS_NUMBER,

            )
        binding.viewCard.inputEditTextCardNumber.hint = (inputCreditCardUIModel.numberHint)
        binding.viewCard.inputEditTextCardExpiryDate.hint = (inputCreditCardUIModel.expiryDateHint)
        binding.viewCard.inputEditTextCardCvc.hint = (inputCreditCardUIModel.cvcHint)

        binding.payButton.setButtonType(paymentRequest.buttonType)
    }

    private fun setupObservers() {
        viewModel.creditCardStatus.observe(viewLifecycleOwner, ::handleOnStatusChanged)
        // viewModel.isFormValid.observe(viewLifecycleOwner, ::handleFormValidationState)
        //   viewModel.formValidator.number.observe(viewLifecycleOwner, ::handleCardNumberValueUpdated)
        /*  Handle Form Validation Errors  */
//    viewModel.formValidator.nameValidator.error.observe(viewLifecycleOwner, ::showInvalidNameErrorMsg)
//    viewModel.formValidator.numberValidator.error.observe(viewLifecycleOwner, ::showInvalidCardNumberErrorMsg)
//    viewModel.formValidator.expiryValidator.error.observe(viewLifecycleOwner, ::showInvalidExpiryErrorMsg)
//    viewModel.formValidator.cvcValidator.error.observe(viewLifecycleOwner, ::showInvalidCVVErrorMsg)
        viewModel.inputFieldsValidatorLiveData.observe(viewLifecycleOwner) { inputFieldUIModel ->
            showInvalidNameErrorMsg(inputFieldUIModel.errorMessage?.nameErrorMsg)
            showInvalidCardNumberErrorMsg(inputFieldUIModel.errorMessage?.numberErrorMsg)
            showInvalidCVVErrorMsg(inputFieldUIModel.errorMessage?.cvcErrorMsg)
            handleFormValidationState(inputFieldUIModel.isFormValid)
            showInvalidExpiryErrorMsg(inputFieldUIModel.errorMessage?.expiryDateErrorMsg)
            handleCardNumberValueUpdated(inputFieldUIModel.cardNumber)
        }
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

    private fun handleFormValidationState(isFormValid: Boolean) {
        binding.payButton.shouldDisableButton(isFormValid ?: false)
    }

    private fun handleCardNumberValueUpdated(number: String?) {
        showAllowedCreditCardsInEditText(number.orEmpty(), paymentRequest.allowedNetworks, binding)
    }

    private fun setError() {
        if (
            viewModel.inputFieldsValidatorLiveData.value?.errorMessage?.numberErrorMsg.isNullOrEmpty()
                .not()
            || viewModel.inputFieldsValidatorLiveData.value?.errorMessage?.expiryDateErrorMsg.isNullOrEmpty()
                .not()
            || viewModel.inputFieldsValidatorLiveData.value?.errorMessage?.cvcErrorMsg.isNullOrEmpty()
                .not()

        ) {
            binding.viewCard.errorTextView.text = if (
                viewModel.inputFieldsValidatorLiveData.value?.errorMessage?.numberErrorMsg.isNullOrEmpty()
                    .not()
            )
                viewModel.inputFieldsValidatorLiveData.value?.errorMessage?.numberErrorMsg
            else if (viewModel.inputFieldsValidatorLiveData.value?.errorMessage?.expiryDateErrorMsg.isNullOrEmpty()
                    .not()
            )
                viewModel.inputFieldsValidatorLiveData.value?.errorMessage?.expiryDateErrorMsg
            else if (viewModel.inputFieldsValidatorLiveData.value?.errorMessage?.cvcErrorMsg.isNullOrEmpty()
                    .not()
            )
                viewModel.inputFieldsValidatorLiveData.value?.errorMessage?.cvcErrorMsg
            else ""

            showError()
        } else hideError()
    }

    private fun showError() {
        binding.viewCard.errorTextView.visibility = View.VISIBLE
        binding.viewCard.llContainer.setBackgroundResource(R.drawable.bg_moyasar_edittext_background_error)
        binding.viewCard.labelTextView.gone()
    }

    private fun hideError() {
        binding.viewCard.errorTextView.visibility = View.GONE
        binding.viewCard.llContainer.setBackgroundResource(R.drawable.bg_moyasar_edittext_background)
        binding.viewCard.labelTextView.show()
    }

    private fun showInvalidCVVErrorMsg(errorMsg: String?) {
        ///viewModel.isCvvValid = errorMsg.isNullOrEmpty()
        setError()
    }

    private fun showInvalidExpiryErrorMsg(errorMsg: String?) {
        /// viewModel.isExpiryValid = errorMsg.isNullOrEmpty()
        setError()

    }

    private fun showInvalidCardNumberErrorMsg(errorMsg: String?) {
        /// viewModel.isCardNumValid = errorMsg.isNullOrEmpty()
        setError()
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
        viewCard.root.show()
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
        viewCard.root.hide()
    }


    private fun FragmentPaymentBinding.setupListeners() {
        etCardHolder.inputEditText.afterTextChanged { text ->
            //   viewModel.formValidator.name.value = text?.toString()
            viewModel.creditCardNameChanged(text?.toString().orEmpty())
        }

        viewCard.inputEditTextCardNumber.afterTextChanged { text ->
            // viewModel.formValidator.number.value = text?.toString()
            text?.let {
                viewModel.creditCardNumberChanged(it)
            }
        }

        viewCard.inputEditTextCardExpiryDate.afterTextChanged { text ->
            //  viewModel.formValidator.expiry.value = text?.toString()
            text?.let {
                viewModel.creditCardExpiryChanged(it) { s ->
                    viewCard.inputEditTextCardExpiryDate.setText(s)
                    // Move cursor to the end of the text
                    viewCard.inputEditTextCardExpiryDate.setSelection(s.length)

                }
            }
        }

        viewCard.inputEditTextCardCvc.afterTextChanged { text ->
            // viewModel.formValidator.cvc.value = text?.toString()
            viewModel.creditCardCvcChanged(text)
        }

        etCardHolder.inputEditText.setOnFocusChangeListener { _, hf ->
            viewModel.validateField(
                fieldType = FieldValidation.Name,
                value = etCardHolder.inputEditText.text?.toString().orEmpty(),
                name = etCardHolder.inputEditText.text?.toString().orEmpty(),
                cardNumber = viewCard.inputEditTextCardNumber.text?.toString().orEmpty(),
                hasFocus = hf
            )
        }
        viewCard.inputEditTextCardNumber.setOnFocusChangeListener { _, hf ->
            viewModel.validateField(
                fieldType = FieldValidation.Number,
                value = viewCard.inputEditTextCardNumber.text?.toString().orEmpty(),
                name = etCardHolder.inputEditText.text?.toString().orEmpty(),
                cardNumber = viewCard.inputEditTextCardNumber.text?.toString().orEmpty(),
                hasFocus = hf
            )
        }
        viewCard.inputEditTextCardExpiryDate.setOnFocusChangeListener { _, hf ->
            viewModel.validateField(
                fieldType = FieldValidation.Expiry,
                value = viewCard.inputEditTextCardExpiryDate.text?.toString().orEmpty(),
                name = etCardHolder.inputEditText.text?.toString().orEmpty(),
                cardNumber = viewCard.inputEditTextCardNumber.text?.toString().orEmpty(),
                hasFocus = hf
            )
        }
        viewCard.inputEditTextCardCvc.setOnFocusChangeListener { _, hf ->
            viewModel.validateField(
                fieldType = FieldValidation.Cvc,
                value = viewCard.inputEditTextCardCvc.text?.toString().orEmpty(),
                name = etCardHolder.inputEditText.text?.toString().orEmpty(),
                cardNumber = viewCard.inputEditTextCardNumber.text?.toString().orEmpty(),
                hasFocus = hf
            )
        }

        payButton.setOnClickListener {
            viewModel.submit()
        }
    }

}

